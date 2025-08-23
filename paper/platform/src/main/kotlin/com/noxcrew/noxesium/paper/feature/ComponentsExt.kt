package com.noxcrew.noxesium.paper.feature

import com.noxcrew.noxesium.api.component.NoxesiumComponentType
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import org.bukkit.entity.Player

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