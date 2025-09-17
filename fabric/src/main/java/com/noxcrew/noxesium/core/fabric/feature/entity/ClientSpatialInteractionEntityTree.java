package com.noxcrew.noxesium.core.fabric.feature.entity;

import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.qib.SpatialTree;
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;

/**
 * Stores a spatial tree with the locations of all interaction entities.
 */
public class ClientSpatialInteractionEntityTree implements SpatialTree {

    private static final int DEFAULT_BRANCHING_FACTOR = 30;

    private final Map<Integer, Entity> pendingEntities = new ConcurrentHashMap<>();
    private final Set<Integer> removedEntities = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean rebuilding = new AtomicBoolean();
    private final AtomicBoolean needsRebuilding = new AtomicBoolean();

    private HashSet<Integer> staticEntities = new HashSet<>();
    private HashSet<AABB> modelContents = new HashSet<>();
    private PRTree<Entity> staticModel = new PRTree<>(new EntityMBRConverter(), DEFAULT_BRANCHING_FACTOR);

    public ClientSpatialInteractionEntityTree() {
        staticModel.load(Set.of());
    }

    /**
     * Returns the contents of the model, if debugging is active.
     */
    public Set<AABB> getModelContents() {
        return modelContents;
    }

    /**
     * Returns the state of [entity] in this tree.
     */
    @Nullable
    public String getSpatialTreeState(Entity entity) {
        if (pendingEntities.containsKey(entity.getId())) {
            return "pending";
        }
        if (removedEntities.contains(entity.getId())) {
            return "removed";
        }
        if (staticEntities.contains(entity.getId())) {
            return "static";
        }
        return null;
    }

    /**
     * Rebuilds the model if applicable.
     */
    public void rebuild() {
        if (!needsRebuilding.get()) return;
        if (!rebuilding.compareAndSet(false, true)) return;
        try {
            var world = Minecraft.getInstance().level;
            if (world == null) return;

            var converter = new EntityMBRConverter();
            var newModel = new PRTree<>(converter, DEFAULT_BRANCHING_FACTOR);

            var oldStaticEntities = staticEntities.size();
            var newStaticEntities = new HashSet<>(staticEntities);
            var addedEntities = new HashSet<>(pendingEntities.keySet());
            var removingEntities = new HashSet<>(removedEntities);
            needsRebuilding.set(false);

            newStaticEntities.addAll(addedEntities);
            newStaticEntities.removeAll(removingEntities);

            // Determine all actual entities that match the entity ids
            var foundEntities = new HashSet<Entity>();
            var iterator = newStaticEntities.iterator();
            while (iterator.hasNext()) {
                var id = iterator.next();
                var entity = world.getEntity(id);
                if (entity == null) {
                    iterator.remove();
                } else {
                    foundEntities.add(entity);
                }
            }
            newModel.load(foundEntities);

            staticModel = newModel;
            staticEntities = newStaticEntities;
            pendingEntities.keySet().removeAll(addedEntities);
            removedEntities.removeAll(removingEntities);

            if (NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging) {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance()
                            .getChatListener()
                            .handleSystemMessage(
                                    Component.literal("§eRebuilt spatial model, before: §f[" + oldStaticEntities + ", "
                                            + addedEntities.size() + ", " + removingEntities.size() + "]§e, after: §f["
                                            + staticEntities.size() + ", " + pendingEntities.size() + ", "
                                            + removedEntities.size() + "]"),
                                    false);
                }

                var newContents = new HashSet<AABB>();
                for (var entity : newStaticEntities) {
                    var fetched = world.getEntity(entity);
                    if (fetched == null) continue;
                    newContents.add(fetched.getBoundingBox());
                }
                modelContents = newContents;
            }
        } finally {
            rebuilding.set(false);
        }
    }

    /**
     * Returns all entities that clip [hitbox].
     */
    @Override
    public HashSet<Entity> findEntities(AABB hitbox) {
        double[] values = {hitbox.minX, hitbox.maxX, hitbox.minY, hitbox.maxY, hitbox.minZ, hitbox.maxZ};
        var mbr = new SimpleMBR(values);
        var collisions = new HashSet<Entity>();

        // Go through the model but ignoring removed entities
        if (removedEntities.isEmpty()) {
            for (var entity : staticModel.find(mbr)) {
                collisions.add(entity);
            }
        } else {
            for (var entity : staticModel.find(mbr)) {
                if (removedEntities.contains(entity.getId())) continue;
                collisions.add(entity);
            }
        }

        // Go through all pending entities that are not yet
        // in the model
        for (var entity : pendingEntities.values()) {
            if (entity.getBoundingBox().intersects(hitbox)) {
                collisions.add(entity);
            }
        }

        return collisions;
    }

    /**
     * Updates the current position of [entity] to [aabb].
     */
    public void update(Interaction entity) {
        if (entity.isRemoved() || !entity.noxesium$hasComponent(CommonEntityComponentTypes.QIB_BEHAVIOR)) {
            remove(entity);
            return;
        }

        removedEntities.remove(entity.getId());
        if (!staticEntities.contains(entity.getId())) {
            pendingEntities.put(entity.getId(), entity);
        }
        needsRebuilding.set(true);
    }

    /**
     * Removes a given [entity] from the tree.
     */
    public void remove(Interaction entity) {
        pendingEntities.remove(entity.getId());
        if (staticEntities.contains(entity.getId())) {
            removedEntities.add(entity.getId());
        }
        needsRebuilding.set(true);
    }

    /**
     * Clears all stored information.
     */
    public void clear() {
        pendingEntities.clear();
        removedEntities.addAll(staticEntities);
        needsRebuilding.set(true);
    }
}
