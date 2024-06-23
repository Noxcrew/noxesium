package com.noxcrew.noxesium.paper.api.rule

import com.noxcrew.noxesium.api.protocol.rule.ServerRule
import net.minecraft.network.FriendlyByteBuf
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftItemStack
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
        buffer.writeJsonWithCodec(net.minecraft.world.item.ItemStack.CODEC, CraftItemStack.asNMSCopy(value))
    }
}

/** A server rule that stores an item stack. */
public class ItemStackListServerRule(
    player: Player,
    index: Int,
    default: List<ItemStack> = emptyList(),
) : RemoteServerRule<List<ItemStack>>(player, index, default) {

    override fun write(value: List<ItemStack>, buffer: FriendlyByteBuf) {
        buffer.writeVarInt(value.size)
        value.forEach {
            buffer.writeJsonWithCodec(net.minecraft.world.item.ItemStack.CODEC, CraftItemStack.asNMSCopy(it))
        }
    }
}
