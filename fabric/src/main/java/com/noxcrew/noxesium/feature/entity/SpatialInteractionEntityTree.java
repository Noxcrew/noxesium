package com.noxcrew.noxesium.feature.entity;

import com.noxcrew.noxesium.NoxesiumMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Stores a spatial tree with the locations of all interaction entities.
 */
public class SpatialInteractionEntityTree {

    private static final int DEFAULT_BRANCHING_FACTOR = 30;

    private static final Map<Integer, Entity> pendingEntities = new ConcurrentHashMap<>();
    private static final Set<Integer> removedEntities = ConcurrentHashMap.newKeySet();
    private static final AtomicBoolean rebuilding = new AtomicBoolean();
    private static final AtomicBoolean needsRebuilding = new AtomicBoolean();

    private static HashSet<Integer> staticEntities = new HashSet<>();
    private static HashSet<AABB> modelContents = new HashSet<>();
    private static PRTree<Entity> staticModel = new PRTree<>(new EntityMBRConverter(), DEFAULT_BRANCHING_FACTOR);

    static {
        staticModel.load(Set.of());
    }

    /**
     * Returns the contents of the model, if debugging is active.
     */
    public static Set<AABB> getModelContents() {
        return modelContents;
    }

    /**
     * Returns the state of [entity] in this tree.
     */
    @Nullable
    public static String getSpatialTreeState(Entity entity) {
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
    public static void rebuild() {
        if (rebuilding.get()) return;
        if (!needsRebuilding.get()) return;

        rebuilding.set(true);
        try {
            var world = Minecraft.getInstance().level;
            if (world == null) return;

            var converter = new EntityMBRConverter();
            var newModel = new PRTree<>(converter, DEFAULT_BRANCHING_FACTOR);

            var oldStaticEntities = staticEntities.size();
            var newStaticEntities = new HashSet<>(staticEntities);
            var addedEntities = new HashSet<>(pendingEntities.keySet());
            var removingEntities = new HashSet<>(removedEntities);
            removingEntities.removeAll(addedEntities);
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
            removedEntities.removeAll(addedEntities);
            removedEntities.removeAll(removingEntities);

            if (NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging) {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().getChatListener().handleSystemMessage(
                            Component.literal("§eRebuilt spatial model, before: §f[" + oldStaticEntities + ", " + addedEntities.size() + ", " + removingEntities.size() + "]§e, after: §f[" + staticEntities.size() + ", " + pendingEntities.size() + ", " + removedEntities.size() + "]"),
                            false
                    );
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
    public static HashSet<Entity> findEntities(AABB hitbox) {
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
    public static void update(Interaction entity) {
        if (!entity.noxesium$isInWorld()) return;
        if (entity.isRemoved()) {
            remove(entity);
            return;
        }

        removedEntities.add(entity.getId());
        pendingEntities.put(entity.getId(), entity);
        needsRebuilding.set(true);
    }

    /**
     * Removes a given [entity] from the tree.
     */
    public static void remove(Interaction entity) {
        if (!entity.noxesium$isInWorld()) return;
        pendingEntities.remove(entity.getId());
        removedEntities.add(entity.getId());
        needsRebuilding.set(true);
    }

    /**
     * Clears all stored information.
     */
    public static void clear() {
        pendingEntities.clear();
        removedEntities.addAll(staticEntities);
        needsRebuilding.set(true);
    }
}
