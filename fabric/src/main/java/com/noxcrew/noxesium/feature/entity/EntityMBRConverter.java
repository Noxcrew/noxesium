package com.noxcrew.noxesium.feature.entity;

import net.minecraft.world.entity.Entity;
import org.khelekore.prtree.MBRConverter;

/**
 * Converts from an entity to an MBR.
 */
public class EntityMBRConverter implements MBRConverter<Entity> {

    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public double getMin(int i, Entity entity) {
        var aabb = entity.getBoundingBox();
        return i == 0 ? aabb.minX : i == 1 ? aabb.minY : aabb.minZ;
    }

    @Override
    public double getMax(int i, Entity entity) {
        var aabb = entity.getBoundingBox();
        return i == 0 ? aabb.maxX : i == 1 ? aabb.maxY : aabb.maxZ;
    }
}
