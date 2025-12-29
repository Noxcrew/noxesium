package com.noxcrew.noxesium.paper.feature.qib

import com.noxcrew.noxesium.core.nms.feature.qib.SpatialTree
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes
import com.noxcrew.noxesium.paper.component.getNoxesiumComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB

/**
 * Stores a spatial tree with the locations of all interaction entities.
 */
public class ServerSpatialInteractionEntityTree(
    private val level: ServerLevel,
) : SpatialTree(ServerEntityMBRConverter()) {
    init {
        staticModel.load(emptyList())
    }

    override fun getEntity(entityId: Int): Entity? = level.entities.get(entityId)

    override fun getHitbox(entity: Entity): AABB {
        entity.getNoxesiumComponent(CommonEntityComponentTypes.HITBOX_OVERRIDE)?.also { override ->
            val position = entity.position()
            val halfX = override.x() / 2f
            val halfZ = override.z() / 2f
            return AABB(
                position.x - halfX,
                position.y,
                position.z - halfZ,
                position.x + halfX,
                position.y + override.y(),
                position.z + halfZ,
            )
        }
        return super.getHitbox(entity)
    }
}
