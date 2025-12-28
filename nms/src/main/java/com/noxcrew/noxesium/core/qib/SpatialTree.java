package com.noxcrew.noxesium.core.qib;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;

/**
 * The basis for a spatial tree that can be used to find colliding interaction entities.
 */
public abstract class SpatialTree {
    protected static final int DEFAULT_BRANCHING_FACTOR = 30;

    protected final Map<Integer, AABB> pendingEntities = new ConcurrentHashMap<>();
    protected final Set<Integer> removedEntities = ConcurrentHashMap.newKeySet();
    protected final AtomicBoolean rebuilding = new AtomicBoolean();
    protected final AtomicBoolean needsRebuilding = new AtomicBoolean();
    protected final MBRConverter<Entity> converter;

    protected HashSet<Integer> staticEntities = new HashSet<>();
    protected PRTree<Entity> staticModel;

    public SpatialTree(MBRConverter<Entity> converter) {
        this.converter = converter;
        this.staticModel = new PRTree<>(converter, DEFAULT_BRANCHING_FACTOR);
    }

    /**
     * Returns all static entity ids in the model.
     */
    public HashSet<Integer> getStaticEntities() {
        return staticEntities;
    }

    /**
     * Returns all pending entity ids for the model.
     */
    public Map<Integer, AABB> getPendingEntities() {
        return pendingEntities;
    }

    /**
     * Returns all entity ids still in the static list but
     * about to be removed.
     */
    public Set<Integer> getRemovedEntities() {
        return removedEntities;
    }

    /**
     * Returns the entity with the given id.
     */
    @Nullable
    public abstract Entity getEntity(int entityId);

    /**
     * Rebuilds the model if applicable.
     */
    public void rebuild() {
        if (!needsRebuilding.get()) return;
        if (!rebuilding.compareAndSet(false, true)) return;
        try {
            var newModel = new PRTree<>(converter, DEFAULT_BRANCHING_FACTOR);
            var newStaticEntities = new HashSet<>(staticEntities);
            var addedEntities = new HashSet<>(pendingEntities.keySet());
            var removingEntities = new HashSet<>(removedEntities);

            // Unset rebuilding as we are currently performing it
            needsRebuilding.set(false);

            newStaticEntities.addAll(addedEntities);
            newStaticEntities.removeAll(removingEntities);

            // Determine all actual entities that match the entity ids
            var foundEntities = new HashSet<Entity>();
            var iterator = newStaticEntities.iterator();
            while (iterator.hasNext()) {
                var id = iterator.next();
                var entity = getEntity(id);
                if (entity == null || entity.isRemoved()) {
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
        } finally {
            rebuilding.set(false);
        }
    }

    /**
     * Returns the bounding box of an entity.
     */
    public AABB getHitbox(Entity entity) {
        return entity.getBoundingBox();
    }

    /**
     * Returns all entities that clip [hitbox].
     */
    public Set<Entity> findEntities(AABB hitbox) {
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
        for (var pair : pendingEntities.entrySet()) {
            if (pair.getValue().intersects(hitbox)) {
                // Look up the entity only if we intersect the hitbox, which should be comparatively uncommon!
                var entity = getEntity(pair.getKey());
                if (entity != null && !entity.isRemoved()) {
                    collisions.add(entity);
                }
            }
        }

        return collisions;
    }

    /**
     * Updates the current position of [entity] to [aabb].
     */
    public void update(Interaction entity) {
        // If the entity is no longer in the world, remove it from the model!
        if (entity.isRemoved()) {
            remove(entity);
            return;
        }

        // Undo the removal if it's queued up and then ensure it's re-added to the model,
        // or add it if's not already in the model itself. If it's already in the model then
        // we trigger a rebuild below to update the position.
        if (removedEntities.remove(entity.getId()) || !staticEntities.contains(entity.getId())) {
            pendingEntities.put(entity.getId(), getHitbox(entity));
        }

        // Always queue up a change regardless if we edited the model as the
        // entity moving needs to trigger a rebuild!
        needsRebuilding.set(true);
    }

    /**
     * Removes a given [entity] from the tree.
     */
    public void remove(Interaction entity) {
        // Remove the actor if it's currently pending (it may currently be in the process
        // of being added to the model) or in the model and needs removal
        if (pendingEntities.remove(entity.getId()) != null || staticEntities.contains(entity.getId())) {
            removedEntities.add(entity.getId());
            needsRebuilding.set(true);
        }
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
