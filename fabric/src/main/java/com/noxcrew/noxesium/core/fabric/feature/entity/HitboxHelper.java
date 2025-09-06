package com.noxcrew.noxesium.core.fabric.feature.entity;

import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Helps in getting the hitbox of an entity.
 */
public class HitboxHelper {
    /**
     * Returns the bounding box of an entity based on the given dimensions.
     */
    public static AABB getBoundingBox(Entity entity, Vec3 position) {
        var override = entity.noxesium$getComponent(CommonEntityComponentTypes.HITBOX_OVERRIDE);
        if (override != null) {
            var halfX = override.x / 2;
            var halfZ = override.z / 2;
            return new AABB(
                    position.x - halfX,
                    position.y,
                    position.z - halfZ,
                    position.x + halfX,
                    position.y + override.y,
                    position.z + halfZ);
        }
        return null;
    }
}
