package com.noxcrew.noxesium.paper.api.network

import com.noxcrew.noxesium.paper.api.network.serverbound.ServerboundNoxesiumPacket
import net.kyori.adventure.key.Key
import net.minecraft.network.FriendlyByteBuf
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/** A type of packet. */
public class ServerboundPacketType<T : ServerboundNoxesiumPacket>(
    /** The id of this packet. */
    id: String,
    /** A function that reads this packet from a buffer. */
    public val reader: ((FriendlyByteBuf) -> T)? = null,
) : PacketType<T>(id) {

    private val updateListeners = ConcurrentHashMap.newKeySet<Pair<WeakReference<Any>, Any.(T, Player) -> Unit>>()

    /** Handles a new packet from [player]. */
    public fun handle(player: Player, packet: T) {
        val iterator = updateListeners.iterator()
        while (iterator.hasNext()) {
            val (reference, consumer) = iterator.next()
            val obj = reference.get()
            if (obj == null) {
                iterator.remove()
                continue
            }
            obj.apply {
                consumer(packet, player)
            }
        }
    }

    /**
     * Adds a new listener to this trigger. Garbage collection of this listener is
     * tied to the lifetime of [reference]. The [listener] itself should only ever
     * reference the passed instance of [reference] and not [reference] directly
     * to avoid situations where the existence of [listener] holds the [reference]
     * captive, preventing it from being garbage collected.
     */
    public fun <R : Any> addListener(reference: R, listener: R.(T, Player) -> Unit) {
        updateListeners.removeIf { it.first.get() == null }
        updateListeners.add(WeakReference(reference) as WeakReference<Any> to listener as (Any.(T, Player) -> Unit))
    }
}
