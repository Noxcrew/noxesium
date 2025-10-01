package com.noxcrew.noxesium.paper.component

import com.noxcrew.noxesium.api.NoxesiumReferences
import com.noxcrew.noxesium.api.component.MutableNoxesiumComponentHolder
import com.noxcrew.noxesium.api.component.NoxesiumEntityManager
import com.noxcrew.noxesium.api.nms.serialization.ComponentSerializerRegistry
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries
import com.noxcrew.noxesium.paper.feature.hasAnyNoxesiumComponent
import net.kyori.adventure.key.Key
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.world.entity.Entity
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer
import kotlin.jvm.optionals.getOrNull

/**
 * Manages entities on NMS platforms, storing their data into NBT using Bukkit's public values.
 */
public class PaperEntityManager public constructor() : NoxesiumEntityManager<Entity, MutableNoxesiumComponentHolder>() {
    override fun hasComponents(entity: Entity): Boolean = entity.bukkitEntity.persistentDataContainer.hasAnyNoxesiumComponent()

    override fun loadComponentHolder(entity: Entity): MutableNoxesiumComponentHolder = EntityComponentHolder(entity).apply {
        // Go through the data stored in the Bukkit data container and load it onto the holder
        val craft = entity.bukkitEntity.persistentDataContainer as CraftPersistentDataContainer
        val tag = craft.getTag(NoxesiumReferences.COMPONENT_NAMESPACE) as? CompoundTag ?: return@apply
        tag.entrySet().forEach { (id, value) ->
            val component = NoxesiumRegistries.ENTITY_COMPONENTS.getByKey(Key.key(id)) ?: return@forEach
            val codec = ComponentSerializerRegistry.getSerializers(NoxesiumRegistries.ENTITY_COMPONENTS, component) ?: return@forEach
            val result = codec.serializers().codec.decode(NbtOps.INSTANCE, value)
            if (result.hasResultOrPartial()) {
                val raw = result.resultOrPartial()?.getOrNull()?.first ?: return@forEach
                setInternalComponent(component, raw)
            }
        }
    }
}
