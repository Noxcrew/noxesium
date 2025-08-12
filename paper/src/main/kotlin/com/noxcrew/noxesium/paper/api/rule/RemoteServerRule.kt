package com.noxcrew.noxesium.paper.api.rule

import com.noxcrew.noxesium.api.protocol.rule.ServerRule
import com.noxcrew.noxesium.api.qib.QibDefinition
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.awt.Color
import java.util.Optional

/**
 * The server-side implementation of a server rule supported by Noxesium.
 */
public abstract class RemoteServerRule<T : Any>(
    private val index: Int,
    private val default: T,
) : ServerRule<T, RegistryFriendlyByteBuf>() {
    private var value: T = default
    internal var changePending: Boolean = false

    override fun read(buffer: RegistryFriendlyByteBuf): T =
        throw UnsupportedOperationException("Cannot read a server-side server rule from a buffer")

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
    index: Int,
    default: Boolean = false,
) : RemoteServerRule<Boolean>(index, default) {
    override fun write(value: Boolean, buffer: RegistryFriendlyByteBuf,) {
        buffer.writeBoolean(value)
    }
}

/** A server rule that stores an integer value. */
public class IntServerRule(
    index: Int,
    default: Int = 0,
) : RemoteServerRule<Int>(index, default) {
    override fun write(value: Int, buffer: RegistryFriendlyByteBuf,) {
        buffer.writeVarInt(value)
    }
}

/** A server rule that stores a double value. */
public class DoubleServerRule(
    index: Int,
    default: Double = 0.0,
) : RemoteServerRule<Double>(index, default) {
    override fun write(value: Double, buffer: RegistryFriendlyByteBuf,) {
        buffer.writeDouble(value)
    }
}

/** A server rule that stores a string value. */
public class StringServerRule(
    index: Int,
    default: String,
) : RemoteServerRule<String>(index, default) {
    override fun write(value: String, buffer: RegistryFriendlyByteBuf,) {
        buffer.writeUtf(value)
    }
}

/** A server rule that stores a list of string value. */
public class StringListServerRule(
    index: Int,
    default: List<String> = emptyList(),
) : RemoteServerRule<List<String>>(index, default) {
    override fun write(value: List<String>, buffer: RegistryFriendlyByteBuf,) {
        buffer.writeCollection(value, FriendlyByteBuf::writeUtf)
    }
}

/** A server rule that stores an item stack. */
public class ItemStackListServerRule(
    index: Int,
    default: List<ItemStack> = emptyList(),
) : RemoteServerRule<List<ItemStack>>(index, default) {
    override fun write(value: List<ItemStack>, buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(value) { _, item -> ItemStackServerRule.write(buffer, Bukkit.getOnlinePlayers().first(), item) } // TODO pass down versioning info
    }
}

/** A server rule that stores an optional color value. */
public class ColorServerRule(
    index: Int,
    default: Optional<Color> = Optional.empty(),
) : RemoteServerRule<Optional<Color>>(index, default) {
    override fun write(value: Optional<Color>, buffer: RegistryFriendlyByteBuf,) {
        buffer.writeOptional(value) { buf, color -> buf.writeVarInt(color.rgb) }
    }
}

/** A server rule that stores an optional enum value. */
public class OptionalEnumServerRule<T : Enum<T>>(
    index: Int,
    default: Optional<T> = Optional.empty(),
) : RemoteServerRule<Optional<T>>(index, default) {
    override fun write(value: Optional<T>, buffer: RegistryFriendlyByteBuf,) {
        buffer.writeOptional(value, FriendlyByteBuf::writeEnum)
    }
}

/** A server rule that stores qib behavior. */
public class QibBehaviorServerRule(
    index: Int,
    default: Map<String, QibDefinition> = emptyMap(),
) : RemoteServerRule<Map<String, QibDefinition>>(index, default) {
    override fun write(value: Map<String, QibDefinition>, buffer: RegistryFriendlyByteBuf,) {
        buffer.writeCollection(value.entries) { buf, (key, value) ->
            buf.writeUtf(key)
            buf.writeUtf(QibDefinition.QIB_GSON.toJson(value))
        }
    }
}

/** A server rule that stores a list of integers. */
public class IntListServerRule(
    index: Int,
    default: List<Int> = emptyList(),
) : RemoteServerRule<List<Int>>(index, default) {
    override fun write(value: List<Int>, buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(value, FriendlyByteBuf::writeVarInt)
    }
}
