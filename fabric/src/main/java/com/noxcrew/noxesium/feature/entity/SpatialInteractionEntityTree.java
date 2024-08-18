package com.noxcrew.noxesium.feature.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Stores a spatial tree with the locations of all interaction entities.
 */
public class SpatialInteractionEntityTree {

    private static final int DEFAULT_BRANCHING_FACTOR = 30;

    private static final Set<Entity> pendingEntities = ConcurrentHashMap.newKeySet();
    private static final Set<Entity> removedEntities = ConcurrentHashMap.newKeySet();
    private static final AtomicBoolean rebuilding = new AtomicBoolean();
    private static final AtomicBoolean needsRebuilding = new AtomicBoolean();

    private static HashSet<Entity> staticEntities = new HashSet<>();
    private static PRTree<Entity> staticModel = new PRTree<>(new EntityMBRConverter(), DEFAULT_BRANCHING_FACTOR);

    static {
        staticModel.load(Set.of());
    }

    /**
     * Rebuilds the model if applicable.
     */
    public static void rebuild() {
        if (rebuilding.get()) return;
        if (!needsRebuilding.get()) return;

        rebuilding.set(true);
        try {
            var newModel = new PRTree<>(new EntityMBRConverter(), DEFAULT_BRANCHING_FACTOR);

            var newStaticEntities = new HashSet<>(staticEntities);
            var addedEntities = new HashSet<>(pendingEntities);
            var removingEntities = new HashSet<>(removedEntities);
            needsRebuilding.set(false);

            newStaticEntities.addAll(addedEntities);
            newStaticEntities.removeAll(removingEntities);
            newModel.load(newStaticEntities);

            staticModel = newModel;
            staticEntities = newStaticEntities;
            pendingEntities.removeAll(addedEntities);
            removedEntities.removeAll(removingEntities);
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
                if (removedEntities.contains(entity)) continue;
                collisions.add(entity);
            }
        }

        // Go through all pending entities that are not yet
        // in the model
        for (var entity : pendingEntities) {
            if (entity.getBoundingBox().intersects(hitbox)) {
                collisions.add(entity);
            }
        }

        return collisions;
    }

    /**
     * Updates the current position of [entity] to [aabb].
     */
    public static void update(Entity entity) {
        removedEntities.remove(entity);

        if (!staticEntities.contains(entity)) {
            pendingEntities.add(entity);
        }

        needsRebuilding.set(true);
    }

    /**
     * Removes a given [entity] from the tree.
     */
    public static void remove(Entity entity) {
        pendingEntities.remove(entity);

        if (staticEntities.contains(entity)) {
            removedEntities.add(entity);
        }

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
