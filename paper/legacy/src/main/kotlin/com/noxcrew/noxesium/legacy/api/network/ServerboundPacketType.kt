package com.noxcrew.noxesium.legacy.api.network

import com.github.benmanes.caffeine.cache.Caffeine
import com.noxcrew.noxesium.paper.api.network.serverbound.ServerboundNoxesiumPacket
import net.minecraft.network.RegistryFriendlyByteBuf
import org.bukkit.entity.Player

/** A type of packet. */
public class ServerboundPacketType<T : ServerboundNoxesiumPacket>(
    /** The id of this packet. */
    id: String,
    /** A function that reads this packet from a buffer. */
    public val reader: ((RegistryFriendlyByteBuf, Player, Int) -> T)? = null,
) : PacketType<T>(id) {
    private val updateListeners =
        Caffeine
            .newBuilder()
            .weakKeys()
            .build<Any, MutableList<Any.(T, Player) -> Unit>>()
            .asMap()

    /** Handles a new packet from [player]. */
    public fun handle(player: Player, packet: T,) {
        updateListeners.forEach { (ref, listeners) ->
            listeners.forEach { ref.it(packet, player) }
        }
    }

    /**
     * Adds a new listener to this trigger. Garbage collection of this listener is
     * tied to the lifetime of [reference]. The [listener] itself should only ever
     * reference the passed instance of [reference] and not [reference] directly
     * to avoid situations where the existence of [listener] holds the [reference]
     * captive, preventing it from being garbage collected.
     */
    public fun <R : Any> addListener(reference: R, listener: R.(T, Player) -> Unit,) {
        @Suppress("UNCHECKED_CAST")
        updateListeners
            .computeIfAbsent(reference) {
                mutableListOf()
            }.add(listener as (Any.(T, Player) -> Unit))
    }
}
