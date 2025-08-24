package com.noxcrew.noxesium.paper.feature

import com.noxcrew.noxesium.api.NoxesiumReferences
import com.noxcrew.noxesium.api.component.MutableNoxesiumComponentHolder
import com.noxcrew.noxesium.api.component.NoxesiumComponentType
import com.noxcrew.noxesium.api.component.NoxesiumEntityManager
import com.noxcrew.noxesium.api.nms.component.NoxesiumComponentHelper
import com.noxcrew.noxesium.api.nms.serialization.ComponentSerializerRegistry
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.world.level.block.entity.BlockEntity
import org.bukkit.block.TileState
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import kotlin.jvm.optionals.getOrNull
import net.minecraft.world.entity.Entity as NmsEntity

/** Returns the Noxesium player object for this player. */
public val Player.noxesiumPlayer: NoxesiumServerPlayer?
    get() = NoxesiumPlayerManager.getInstance().getPlayer(uniqueId)

/** Returns the given component data on this holder. */
public fun <T> Player.getNoxesiumComponent(component: NoxesiumComponentType<T>): T? =
    noxesiumPlayer?.gameComponents?.`noxesium$getComponent`(component)

/** Returns whether this holder has the given component set. */
public fun Player.hasNoxesiumComponent(component: NoxesiumComponentType<*>): Boolean =
    noxesiumPlayer?.gameComponents?.`noxesium$hasComponent`(component) == true

/** Sets the given component on this holder. */
public fun <T> Player.setNoxesiumComponent(component: NoxesiumComponentType<T>, value: T?) {
    noxesiumPlayer?.gameComponents?.`noxesium$setComponent`(component, value)
}

/** Returns the given component data on this holder. */
public fun <T> Entity.getNoxesiumComponent(component: NoxesiumComponentType<T>): T? =
    (this as CraftEntity).handle.getNoxesiumComponent(component)

/** Returns whether this holder has the given component set. */
public fun Entity.hasNoxesiumComponent(component: NoxesiumComponentType<*>): Boolean =
    (this as CraftEntity).handle.hasNoxesiumComponent(component)

/** Sets the given component on this holder. */
public fun <T> Entity.setNoxesiumComponent(component: NoxesiumComponentType<T>, value: T?) {
    (this as CraftEntity).handle.setNoxesiumComponent(component, value)
}

/** Returns the Noxesium component holder for this entity. */
public val NmsEntity.componentHolder: MutableNoxesiumComponentHolder
    get() = NoxesiumEntityManager.getInstance<NmsEntity, MutableNoxesiumComponentHolder>().getComponentHolder(this)

/** Returns the Noxesium component holder for this entity. */
public val NmsEntity.componentHolderIfPresent: MutableNoxesiumComponentHolder?
    get() = NoxesiumEntityManager.getInstance<NmsEntity, MutableNoxesiumComponentHolder>()
        .getComponentHolderIfPresent(this)

/** Returns the given component data on this holder. */
public fun <T> NmsEntity.getNoxesiumComponent(component: NoxesiumComponentType<T>): T? =
    componentHolderIfPresent?.`noxesium$getComponent`(component)

/** Returns whether this holder has the given component set. */
public fun NmsEntity.hasNoxesiumComponent(component: NoxesiumComponentType<*>): Boolean =
    componentHolderIfPresent?.`noxesium$hasComponent`(component) == true

/** Sets the given component on this holder. */
public fun <T> NmsEntity.setNoxesiumComponent(component: NoxesiumComponentType<T>, value: T?) {
    if (value == null) {
        componentHolderIfPresent?.`noxesium$setComponent`(component, null)
    } else {
        componentHolder.`noxesium$setComponent`(component, value)
    }
}

/**
 * Returns the given component data on this holder. These values are not cached and will
 * be re-serialized on every fetch. Please cache the result of this function whenever
 * possible.
 */
public fun <T> ItemStack.getNoxesiumComponent(type: NoxesiumComponentType<T>): T? =
    NoxesiumComponentHelper.getNoxesiumComponent(NoxesiumRegistries.ITEM_COMPONENTS, CraftItemStack.unwrap(this), type)

/** Returns whether this holder has the given component set. */
public fun ItemStack.hasNoxesiumComponent(type: NoxesiumComponentType<*>): Boolean =
    NoxesiumComponentHelper.hasNoxesiumComponent(CraftItemStack.unwrap(this), type)

/** Sets the given component on this holder. */
public fun <T> ItemStack.setNoxesiumComponent(type: NoxesiumComponentType<T>, value: T?): Unit =
    NoxesiumComponentHelper.setNoxesiumComponent(NoxesiumRegistries.ITEM_COMPONENTS, CraftItemStack.unwrap(this), type, value)

/**
 * Returns the given component data on this holder. These values are not cached and will
 * be re-serialized on every fetch. Please cache the result of this function whenever
 * possible.
 */
public fun <T> BlockEntity.getNoxesiumComponent(type: NoxesiumComponentType<T>): T? = persistentDataContainer.getNoxesiumComponent(type)

/** Returns whether this holder has the given component set. */
public fun BlockEntity.hasNoxesiumComponent(type: NoxesiumComponentType<*>): Boolean = persistentDataContainer.hasNoxesiumComponent(type)

/** Sets the given component on this holder. */
public fun <T> BlockEntity.setNoxesiumComponent(type: NoxesiumComponentType<T>, value: T?) {
    if (persistentDataContainer.setNoxesiumComponent(type, value)) setChanged()
}

/**
 * Returns the given component data on this holder. These values are not cached and will
 * be re-serialized on every fetch. Please cache the result of this function whenever
 * possible.
 */
public fun <T> TileState.getNoxesiumComponent(type: NoxesiumComponentType<T>): T? = persistentDataContainer.getNoxesiumComponent(type)

/** Returns whether this holder has the given component set. */
public fun TileState.hasNoxesiumComponent(type: NoxesiumComponentType<*>): Boolean = persistentDataContainer.hasNoxesiumComponent(type)

/** Sets the given component on this holder. */
public fun <T> TileState.setNoxesiumComponent(type: NoxesiumComponentType<T>, value: T?) {
    if (persistentDataContainer.setNoxesiumComponent(type, value)) update(true, false)
}

/** Returns whether this holder has any Noxesium components. */
internal fun PersistentDataContainer.hasAnyNoxesiumComponent(): Boolean {
    val craft = this as CraftPersistentDataContainer
    return craft.getTag(NoxesiumReferences.COMPONENT_NAMESPACE) != null
}

/**
 * Returns the given component data on this holder. These values are not cached and will
 * be re-serialized on every fetch. Please cache the result of this function whenever
 * possible.
 */
internal fun <T> PersistentDataContainer.getNoxesiumComponent(type: NoxesiumComponentType<T>): T? {
    val codec = ComponentSerializerRegistry.getSerializers(NoxesiumRegistries.BLOCK_ENTITY_COMPONENTS, type) ?: return null
    val craft = this as CraftPersistentDataContainer
    val tag = craft.getTag(NoxesiumReferences.COMPONENT_NAMESPACE) as? CompoundTag ?: return null
    val value = tag.get(type.id.asString()) ?: return null
    val result = codec.codec.decode(NbtOps.INSTANCE, value)
    if (result.hasResultOrPartial()) {
        return result.resultOrPartial()?.getOrNull()?.first
    }
    return null
}

/** Returns whether this holder has the given component set. */
internal fun PersistentDataContainer.hasNoxesiumComponent(type: NoxesiumComponentType<*>): Boolean {
    val craft = this as CraftPersistentDataContainer
    val tag = craft.getTag(NoxesiumReferences.COMPONENT_NAMESPACE) as? CompoundTag ?: return false
    return tag.contains(type.id.asString())
}

/** Sets the given component on this holder. */
internal fun <T> PersistentDataContainer.setNoxesiumComponent(type: NoxesiumComponentType<T>, value: T?): Boolean {
    val codec = ComponentSerializerRegistry.getSerializers(NoxesiumRegistries.BLOCK_ENTITY_COMPONENTS, type) ?: return false
    val craft = this as CraftPersistentDataContainer
    if (value == null) {
        val tag = craft.getTag(NoxesiumReferences.COMPONENT_NAMESPACE) as? CompoundTag ?: return false
        tag.remove(type.id.asString())
        if (tag.isEmpty) {
            craft.raw.remove(type.id.asString())
        } else {
            craft.put(NoxesiumReferences.COMPONENT_NAMESPACE, tag)
        }
        return true
    }

    val result = codec.codec.encodeStart(NbtOps.INSTANCE, value)
    if (result.hasResultOrPartial()) {
        val encoded = result.resultOrPartial()?.getOrNull() ?: return false
        val tag = (craft.getTag(NoxesiumReferences.COMPONENT_NAMESPACE) as? CompoundTag) ?: CompoundTag()
        tag.put(type.id.asString(), encoded)
        craft.put(NoxesiumReferences.COMPONENT_NAMESPACE, tag)
        return true
    }
    return false
}
