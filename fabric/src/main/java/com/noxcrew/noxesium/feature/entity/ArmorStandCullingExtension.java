package com.noxcrew.noxesium.feature.entity;

import net.minecraft.world.phys.AABB;

/**
 * Defines an extension for armor stand culling bounding boxes.
 */
public interface ArmorStandCullingExtension {

    /**
     * Returns the culling bounding box.
     */
    public default AABB noxesium$getCullingBoundingBox() {
        throw new UnsupportedOperationException("Unimplemented");
    }

    /**
     * Updates the culling bounding box.
     */
    public default void noxesium$setCullingBoundingBox(AABB boundingBox) {
    }
}
