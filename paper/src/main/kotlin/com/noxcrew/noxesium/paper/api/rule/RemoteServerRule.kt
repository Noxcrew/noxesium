package com.noxcrew.noxesium.paper.api.rule

import com.noxcrew.noxesium.api.protocol.rule.ServerRule
import net.minecraft.network.FriendlyByteBuf
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * The server-side implementation of a server rule supported by Noxesium.
 */
public abstract class RemoteServerRule<T : Any>(
    private val player: Player,
    private val index: Int,
    private val default: T,
) : ServerRule<T, FriendlyByteBuf>() {

    private var value: T = default
    internal var changePending: Boolean = false

    override fun read(buffer: FriendlyByteBuf): T {
        throw UnsupportedOperationException("Cannot read a server-side server rule from a buffer")
    }

    override fun getValue(): T = value

    override fun setValue(value: T) {
        if (this.value == value) return
        this.value = value
        this.changePending = true
    }

    override fun getDefault(): T = default
    override fun getIndex(): Int = index
}

/** A server rule that stores a boolean value. */
public class BooleanServerRule(
    player: Player,
    index: Int,
    default: Boolean = false,
) : RemoteServerRule<Boolean>(player, index, default) {

    override fun write(value: Boolean, buffer: FriendlyByteBuf) {
        buffer.writeBoolean(value)
    }
}

/** A server rule that stores an integer value. */
public class IntServerRule(
    player: Player,
    index: Int,
    default: Int = 0,
) : RemoteServerRule<Int>(player, index, default) {

    override fun write(value: Int, buffer: FriendlyByteBuf) {
        buffer.writeVarInt(value)
    }
}

/** A server rule that stores a list of string value. */
public class StringListServerRule(
    player: Player,
    index: Int,
    default: List<String> = emptyList(),
) : RemoteServerRule<List<String>>(player, index, default) {

    override fun write(value: List<String>, buffer: FriendlyByteBuf) {
        buffer.writeCollection(value, FriendlyByteBuf::writeUtf)
    }
}

/** A server rule that stores an item stack. */
public class ItemStackServerRule(
    player: Player,
    index: Int,
    default: ItemStack = ItemStack(Material.AIR),
) : RemoteServerRule<ItemStack>(player, index, default) {

    override fun write(value: ItemStack, buffer: FriendlyByteBuf) {
        /*
        As of 1.20.5 the client uses a readJsonWithCodec which does not exist yet in
        Minecraft 1.19.4. It's only possible for the server to serialize this type of
        data in 1.20.5 and later. It also changed in between 1.19.4-1.20.4 so generally
        serializing item stacks is not very stable currently.

        Actual support for this awaits a server-side update to 1.20.5.
         */
        throw UnsupportedOperationException("Unimplemented on 1.19.4")
    }
}
