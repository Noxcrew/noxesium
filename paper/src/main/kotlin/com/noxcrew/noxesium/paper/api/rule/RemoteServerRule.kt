package com.noxcrew.noxesium.paper.api.rule

import com.noxcrew.noxesium.api.protocol.rule.ServerRule
import com.noxcrew.noxesium.api.qib.QibDefinition
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.awt.Color
import java.util.Optional

/**
 * The server-side implementation of a server rule supported by Noxesium.
 */
public abstract class RemoteServerRule<T : Any>(
    private val player: Player?,
    private val index: Int,
    private val default: T,
) : ServerRule<T, RegistryFriendlyByteBuf>() {

    private var value: T = default
    internal var changePending: Boolean = false

    override fun read(buffer: RegistryFriendlyByteBuf): T {
        throw UnsupportedOperationException("Cannot read a server-side server rule from a buffer")
    }

    override fun getValue(): T = value

    override fun setValue(value: T) {
        if (this.value == value) return
        this.value = value
        this.changePending = true
    }

    override fun reset() {
        // We enforce resetting because another
        // server may have had a cached value change!
        this.value = default
        this.changePending = true
    }

    override fun getDefault(): T = default
    override fun getIndex(): Int = index
}

/** A server rule that stores a boolean value. */
public class BooleanServerRule(
    player: Player?,
    index: Int,
    default: Boolean = false,
) : RemoteServerRule<Boolean>(player, index, default) {

    override fun write(value: Boolean, buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(value)
    }
}

/** A server rule that stores an integer value. */
public class IntServerRule(
    player: Player?,
    index: Int,
    default: Int = 0,
) : RemoteServerRule<Int>(player, index, default) {

    override fun write(value: Int, buffer: RegistryFriendlyByteBuf) {
        buffer.writeVarInt(value)
    }
}

/** A server rule that stores a double value. */
public class DoubleServerRule(
    player: Player?,
    index: Int,
    default: Double = 0.0,
) : RemoteServerRule<Double>(player, index, default) {

    override fun write(value: Double, buffer: RegistryFriendlyByteBuf) {
        buffer.writeDouble(value)
    }
}

/** A server rule that stores a string value. */
public class StringServerRule(
    player: Player?,
    index: Int,
    default: String,
) : RemoteServerRule<String>(player, index, default) {

    override fun write(value: String, buffer: RegistryFriendlyByteBuf) {
        buffer.writeUtf(value)
    }
}

/** A server rule that stores a list of string value. */
public class StringListServerRule(
    player: Player?,
    index: Int,
    default: List<String> = emptyList(),
) : RemoteServerRule<List<String>>(player, index, default) {

    override fun write(value: List<String>, buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(value, FriendlyByteBuf::writeUtf)
    }
}

/** A server rule that stores an item stack. */
public class ItemStackServerRule(
    player: Player?,
    index: Int,
    default: ItemStack = ItemStack(Material.AIR),
) : RemoteServerRule<ItemStack>(player, index, default) {

    override fun write(value: ItemStack, buffer: RegistryFriendlyByteBuf) {
        net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, CraftItemStack.asNMSCopy(value))
    }
}

/** A server rule that stores an item stack. */
public class ItemStackListServerRule(
    player: Player?,
    index: Int,
    default: List<ItemStack> = emptyList(),
) : RemoteServerRule<List<ItemStack>>(player, index, default) {

    override fun write(value: List<ItemStack>, buffer: RegistryFriendlyByteBuf) {
        net.minecraft.world.item.ItemStack.OPTIONAL_LIST_STREAM_CODEC.encode(buffer, value.map { CraftItemStack.asNMSCopy(it) })
    }
}

/** A server rule that stores an optional color value. */
public class ColorServerRule(
    player: Player?,
    index: Int,
    default: Optional<Color> = Optional.empty(),
) : RemoteServerRule<Optional<Color>>(player, index, default) {

    override fun write(value: Optional<Color>, buffer: RegistryFriendlyByteBuf) {
        buffer.writeOptional(value) { buf, color -> buf.writeVarInt(color.rgb) }
    }
}

/** A server rule that stores an optional enum value. */
public class OptionalEnumServerRule<T : Enum<T>>(
    player: Player?,
    index: Int,
    default: Optional<T> = Optional.empty(),
) : RemoteServerRule<Optional<T>>(player, index, default) {

    override fun write(value: Optional<T>, buffer: RegistryFriendlyByteBuf) {
        buffer.writeOptional(value, FriendlyByteBuf::writeEnum)
    }
}

/** A server rule that stores qib behavior. */
public class QibBehaviorServerRule(
    player: Player?,
    index: Int,
    default: Map<String, QibDefinition> = emptyMap(),
) : RemoteServerRule<Map<String, QibDefinition>>(player, index, default) {

    override fun write(value: Map<String, QibDefinition>, buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(value.entries) { buf, (key, value) ->
            buf.writeUtf(key)
            buf.writeUtf(QibDefinition.QIB_GSON.toJson(value))
        }
    }
}
