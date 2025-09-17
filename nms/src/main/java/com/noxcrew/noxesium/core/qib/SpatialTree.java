package com.noxcrew.noxesium.core.qib;

import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/**
 * The basis for a spatial tree that can be used to find colliding interaction entities.
 */
public interface SpatialTree {
    /**
     * Returns all entities that clip [hitbox].
     */
    public Set<Entity> findEntities(AABB hitbox);
}
