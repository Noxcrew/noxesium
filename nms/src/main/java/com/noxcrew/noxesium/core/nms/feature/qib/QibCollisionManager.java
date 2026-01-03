package com.noxcrew.noxesium.core.nms.feature.qib;

import com.noxcrew.noxesium.api.feature.qib.QibEffect;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundQibTriggeredPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.key.Key;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Assists in executing the behaviors for qibs for a specific entity.
 */
public abstract class QibCollisionManager {

    protected final Player player;
    protected final SpatialTree spatialTree;
    protected final Map<Key, AtomicInteger> collidingWithTypes = new HashMap<>();
    protected final Map<Entity, AtomicInteger> collidingWithEntities = new HashMap<>();
    protected final Set<Entity> triggeredJump = new HashSet<>();
    protected final List<Pair<AtomicInteger, Pair<Entity, QibEffect>>> pending = new ArrayList<>();

    public QibCollisionManager(@NotNull final Player player, @NotNull final SpatialTree spatialTree) {
        this.player = player;
        this.spatialTree = spatialTree;
    }

    /**
     * Returns the player instance being tracked.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Ticks this manager, updating current collisions and looking for new ones.
     */
    public void tick() {
        // Check if the player is colliding with any interaction entities
        tickEffects();

        // Perform checks between the start and end location
        var from = player.getPosition(0f);
        var to = player.getPosition(1f);

        var diffX = to.x - from.x;
        var diffY = to.y - from.y;
        var diffZ = to.z - from.z;
        var differenceLengthSquared = diffX * diffX + diffY * diffY + diffZ * diffZ;

        // If there's more than 0.5 between the two targets we do intermediate steps
        // to ensure we collide with everything!
        Set<Entity> entities = null;
        if (differenceLengthSquared >= 0.25) {
            var dimensions = player.getDimensions(player.getPose());
            var currentLocation = from;
            var differenceLength = Math.sqrt(differenceLengthSquared);
            var factor = 0.5 / differenceLength;
            var times = Math.ceil(differenceLength / .5) - 1;
            for (int i = 0; i < times; i++) {
                currentLocation = currentLocation.add(diffX * factor, diffY * factor, diffZ * factor);

                if (entities == null) {
                    entities = spatialTree.findEntities(dimensions.makeBoundingBox(currentLocation));
                } else {
                    entities.addAll(spatialTree.findEntities(dimensions.makeBoundingBox(currentLocation)));
                }
            }
        }

        if (entities == null) {
            entities = spatialTree.findEntities(player.getBoundingBox());
        } else {
            entities.addAll(spatialTree.findEntities(player.getBoundingBox()));
        }

        checkForCollisions(entities);
    }

    /**
     * Returns the qib behavior of the given entity.
     */
    @Nullable
    protected abstract Key getQibBehavior(Entity entity);

    /**
     * Triggers when a player uses an item with an attack behavior.
     */
    public void onAttackItemBehavior(Player player, Key behavior) {
        var definition = NoxesiumRegistries.QIB_EFFECTS.getByKey(behavior);
        if (definition == null) return;
        if (definition.onAttack() != null) {
            onQibTriggered(behavior, ServerboundQibTriggeredPacket.Type.ATTACK_BEHAVIOR, player.getId());
            executeBehavior(player, definition.onAttack());
        }
    }

    /**
     * Triggers when a player uses an item with use behavior.
     */
    public void onUseItemBehavior(Player player, Key behavior) {
        var definition = NoxesiumRegistries.QIB_EFFECTS.getByKey(behavior);
        if (definition == null) return;
        if (definition.onUse() != null) {
            onQibTriggered(behavior, ServerboundQibTriggeredPacket.Type.USE_BEHAVIOR, player.getId());
            executeBehavior(player, definition.onUse());
        }
    }

    /**
     * Triggers when a player jumps.
     */
    public void onPlayerJump() {
        // Do not allow jumping while in a vehicle.
        if (player.getVehicle() != null) return;

        for (var entity : collidingWithEntities.keySet()) {
            // Don't trigger jumping twice for the same entity!
            if (triggeredJump.contains(entity)) continue;

            // Check the behavior of the entity
            var behavior = getQibBehavior(entity);
            if (behavior == null) continue;

            // Try to trigger the jump behavior
            var definition = NoxesiumRegistries.QIB_EFFECTS.getByKey(behavior);
            if (definition == null) continue;
            if (definition.onJump() != null) {
                onQibTriggered(behavior, ServerboundQibTriggeredPacket.Type.JUMP, entity.getId());
                executeBehavior(entity, definition.onJump());
                triggeredJump.add(entity);
            }
        }
    }

    /**
     * Ticks down scheduled effects and runs them.
     */
    private void tickEffects() {
        // Increment all timers
        collidingWithTypes.values().forEach(AtomicInteger::incrementAndGet);
        collidingWithEntities.values().forEach(AtomicInteger::incrementAndGet);

        var iterator = pending.iterator();
        while (iterator.hasNext()) {
            var pair = iterator.next();
            if (pair.getKey().decrementAndGet() <= 0) {
                var value = pair.getValue();
                iterator.remove();
                executeBehavior(value.getLeft(), value.getRight());
            }
        }
    }

    /**
     * Checks for collisions with interaction entities.
     */
    private void checkForCollisions(Set<Entity> entities) {
        /*
         *  Ideally we would use vanilla's getEntities method as it uses the local chunks directly to only check against nearby
         *  entities. However, vanilla's implementation only iterates over chunks that the player collides with and then checks
         *  the hitboxes of entities in there, it does not add entities to all chunks they clip. This means that any interaction
         *  entity that clips multiple chunks does not get recognised outside its source chunk.
         *
         *  To solve this we absolutely over-engineer this problem and use a spatial tree structure to find all interaction entities.
         */
        // Determine all current collisions
        var collidingTypes = new ArrayList<Key>();
        for (var entity : entities) {
            // Stop colliding with this entity
            var behavior = getQibBehavior(entity);
            if (behavior == null) continue;

            // Determine this entity's behavior
            var definition = NoxesiumRegistries.QIB_EFFECTS.getByKey(behavior);
            if (definition == null) continue;

            // Try to trigger the entry
            if ((!definition.triggerEnterLeaveOnSwitch() && !collidingWithTypes.containsKey(behavior))
                    || (definition.triggerEnterLeaveOnSwitch() && !collidingWithEntities.containsKey(entity))) {
                if (definition.onEnter() != null) {
                    onQibTriggered(behavior, ServerboundQibTriggeredPacket.Type.ENTER, entity.getId());
                    executeBehavior(entity, definition.onEnter());
                }
            }

            // Always trigger the while inside logic
            if (definition.whileInside() != null) {
                onQibTriggered(behavior, ServerboundQibTriggeredPacket.Type.INSIDE, entity.getId());
                executeBehavior(entity, definition.whileInside());
            }
            collidingTypes.add(behavior);
            collidingWithTypes.putIfAbsent(behavior, new AtomicInteger());
            collidingWithEntities.putIfAbsent(entity, new AtomicInteger());
        }

        // Only keep the types you are still colliding with
        collidingWithTypes.keySet().retainAll(collidingTypes);

        var iterator = collidingWithEntities.keySet().iterator();
        while (iterator.hasNext()) {
            // If you're still colliding with this entity we ignore it
            var collision = iterator.next();
            if (entities.contains(collision)) continue;

            // Remove them from the iterator
            iterator.remove();

            // Remove from triggered jump when you leave it
            triggeredJump.remove(collision);

            // Stop colliding with this entity
            var behavior = getQibBehavior(collision);
            if (behavior == null) continue;

            // Determine this entity's behavior
            var definition = NoxesiumRegistries.QIB_EFFECTS.getByKey(behavior);
            if (definition == null) continue;

            // Execute the behavior if we always do or if you've left this type
            if ((!definition.triggerEnterLeaveOnSwitch() && !collidingWithTypes.containsKey(behavior))
                    || (definition.triggerEnterLeaveOnSwitch() && !collidingWithEntities.containsKey(collision))) {
                if (definition.onLeave() != null) {
                    onQibTriggered(behavior, ServerboundQibTriggeredPacket.Type.LEAVE, collision.getId());
                    executeBehavior(collision, definition.onLeave());
                }
            }
        }
    }

    /**
     * Executes the given behavior.
     */
    protected void executeBehavior(Entity entity, QibEffect effect) {
        switch (effect) {
            case QibEffect.Multiple multiple -> {
                for (var nested : multiple.effects()) {
                    executeBehavior(entity, nested);
                }
            }
            case QibEffect.Stay stay -> {
                var timeSpent = stay.global()
                        ? collidingWithTypes
                                .getOrDefault(getQibBehavior(entity), new AtomicInteger())
                                .get()
                        : collidingWithEntities
                                .getOrDefault(entity, new AtomicInteger())
                                .get();

                if (timeSpent >= stay.ticks()) {
                    executeBehavior(entity, stay.effect());
                }
            }
            case QibEffect.Wait wait -> {
                pending.add(Pair.of(new AtomicInteger(wait.ticks()), Pair.of(entity, wait.effect())));
            }
            case QibEffect.Conditional conditional -> {
                boolean result =
                        switch (conditional.condition()) {
                            case IS_GLIDING -> player.isFallFlying();
                            case IS_RIPTIDING -> player.isAutoSpinAttack();
                            case IS_IN_AIR -> !player.onGround();
                            case IS_ON_GROUND -> player.onGround();
                            case IS_IN_WATER -> player.isInWater();
                            case IS_IN_WATER_OR_RAIN -> player.isInWaterOrRain();
                            case IS_IN_VEHICLE -> player.getVehicle() != null;
                        };

                // Trigger the effect if it matches
                if (result == conditional.value()) {
                    executeBehavior(entity, conditional.effect());
                }
            }
            case QibEffect.Move move -> {
                player.move(MoverType.SELF, new Vec3(move.x(), move.y(), move.z()));
            }
            case QibEffect.SetVelocity setVelocity -> {
                player.setDeltaMovement(setVelocity.x(), setVelocity.y(), setVelocity.z());
                player.needsSync = true;
            }
            case QibEffect.SetVelocityYawPitch setVelocityYawPitch -> {
                var yawRad = Math.toRadians(
                        setVelocityYawPitch.yaw() + (setVelocityYawPitch.yawRelative() ? player.yRotO : 0));
                var pitchRad = Math.toRadians(
                        setVelocityYawPitch.pitch() + (setVelocityYawPitch.pitchRelative() ? player.xRotO : 0));

                var x = -Math.cos(pitchRad) * Math.sin(yawRad);
                var y = -Math.sin(pitchRad);
                var z = Math.cos(pitchRad) * Math.cos(yawRad);
                player.setDeltaMovement(
                        Math.clamp(
                                x * setVelocityYawPitch.strength(),
                                -setVelocityYawPitch.limit(),
                                setVelocityYawPitch.limit()),
                        Math.clamp(
                                y * setVelocityYawPitch.strength(),
                                -setVelocityYawPitch.limit(),
                                setVelocityYawPitch.limit()),
                        Math.clamp(
                                z * setVelocityYawPitch.strength(),
                                -setVelocityYawPitch.limit(),
                                setVelocityYawPitch.limit()));
                player.needsSync = true;
            }
            case QibEffect.ModifyVelocity modifyVelocity -> {
                var current = player.getDeltaMovement();
                player.setDeltaMovement(
                        modifyVelocity.xOp().apply(current.x, modifyVelocity.x()),
                        modifyVelocity.yOp().apply(current.y, modifyVelocity.y()),
                        modifyVelocity.zOp().apply(current.z, modifyVelocity.z()));
                player.needsSync = true;
            }
            case QibEffect.ApplyImpulse applyImpulse -> {
                var direction = applyImpulse.direction();
                var scale = applyImpulse.scale();
                var lookAngle = player.getLookAngle();
                var impulse = lookAngle
                        .addLocalCoordinates(new Vec3(direction.x, direction.y, direction.z))
                        .multiply(new Vec3(scale.x, scale.y, scale.z));
                player.addDeltaMovement(impulse);
                player.needsSync = true;
            }
            case QibEffect.StopGliding stopGliding -> {
                player.stopFallFlying();
            }
            default -> throw new IllegalStateException("Unexpected value: " + effect);
        }
    }

    /**
     * A generic hook called when a qib of a certain type is triggered.
     */
    protected void onQibTriggered(Key behavior, ServerboundQibTriggeredPacket.Type type, int entityId) {}
}
