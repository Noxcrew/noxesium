package com.noxcrew.noxesium.paper.feature.qib

import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes
import com.noxcrew.noxesium.paper.component.getNoxesiumComponent
import net.minecraft.world.entity.Entity
import org.khelekore.prtree.MBRConverter

/**
 * Converts from an entity to an MBR.
 */
public class ServerEntityMBRConverter() : MBRConverter<Entity> {
    override fun getDimensions(): Int = 3

    override fun getMin(i: Int, entity: Entity): Double {
        // Read from the hitbox override if applicable
        entity.getNoxesiumComponent(CommonEntityComponentTypes.HITBOX_OVERRIDE)?.also { override ->
            val position = entity.position()
            val halfX = override.x() / 2f
            val halfZ = override.z() / 2f
            return if (i == 0) {
                position.x - halfX
            } else if (i == 1) {
                position.y
            } else {
                position.z - halfZ
            }
        }

        val aabb = entity.boundingBox
        return if (i == 0) {
            aabb.minX
        } else if (i == 1) {
            aabb.minY
        } else {
            aabb.minZ
        }
    }

    override fun getMax(i: Int, entity: Entity): Double {
        // Read from the hitbox override if applicable
        entity.getNoxesiumComponent(CommonEntityComponentTypes.HITBOX_OVERRIDE)?.also { override ->
            val position = entity.position()
            val halfX = override.x() / 2f
            val halfZ = override.z() / 2f
            return if (i == 0) {
                position.x + halfX
            } else if (i == 1) {
                position.y + override.y()
            } else {
                position.z + halfZ
            }
        }

        val aabb = entity.boundingBox
        return if (i == 0) {
            aabb.maxX
        } else if (i == 1) {
            aabb.maxY
        } else {
            aabb.maxZ
        }
    }
}
