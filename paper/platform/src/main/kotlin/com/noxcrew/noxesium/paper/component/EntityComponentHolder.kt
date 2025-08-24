package com.noxcrew.noxesium.paper.component

import com.noxcrew.noxesium.api.NoxesiumReferences
import com.noxcrew.noxesium.api.component.NoxesiumComponentType
import com.noxcrew.noxesium.api.component.SimpleMutableNoxesiumComponentHolder
import com.noxcrew.noxesium.paper.feature.setNoxesiumComponent
import net.minecraft.world.entity.Entity

/** Holds components for entities. */
public class EntityComponentHolder(
    /** The NMS entity instance for this holder. */
    public val entity: Entity,
) : SimpleMutableNoxesiumComponentHolder() {
    // Store any changes made to this component onto the entity's data! We load back from the entity's
    // data when we recreate the entity component holder in the future.
    override fun <T : Any?> `noxesium$setComponent`(component: NoxesiumComponentType<T>, value: T?) {
        super.`noxesium$setComponent`(component, value)
        entity.bukkitEntity.persistentDataContainer.setNoxesiumComponent(component, value)
    }

    override fun `noxesium$clearComponents`() {
        super.`noxesium$clearComponents`()
        entity.bukkitEntity.persistentDataContainer.raw.remove(NoxesiumReferences.COMPONENT_NAMESPACE)
    }
}
