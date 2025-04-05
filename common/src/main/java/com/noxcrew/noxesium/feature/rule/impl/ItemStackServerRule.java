package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * A server rule that stores an item stack.
 *
 * Implements a custom syntax for writing item stacks as vanilla writes them using codecs
 * which constantly change between versions as registry contents for e.g. data components
 * are not yet synced. This means these rules break every minor version making it very
 * tedious to synchronize between server and client. Instead, we store the items in a
 * custom format that is cross-compatible between versions.
 */
public class ItemStackServerRule extends ClientServerRule<ItemStack> {

    /**
     * Reads an item from the given buffer.
     */
    public static ItemStack readItemStackFromBuffer(RegistryFriendlyByteBuf buffer) {
        var count = buffer.readVarInt();
        if (count <= 0) {
            return ItemStack.EMPTY;
        } else {
            var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(buffer.readUtf()));
            if (item.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                var patch = readDataComponentPatch(buffer);
                return new ItemStack(item.get(), count, patch);
            }
        }
    }

    /**
     * Reads a data component patch from the given buffer.
     */
    public static DataComponentPatch readDataComponentPatch(RegistryFriendlyByteBuf buffer) {
        var components = buffer.readVarInt();
        var emptyComponents = buffer.readVarInt();
        if (components == 0 && emptyComponents == 0) {
            return DataComponentPatch.EMPTY;
        }
        var builder = DataComponentPatch.builder();
        for (int i = 0; i < components; i++) {
            var type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.parse(buffer.readUtf()));
            if (type.isEmpty()) return builder.build(); // If any component is unknown we have to stop parsing!
            decodeComponent(buffer, type.get().value(), builder);
        }
        for (int i = 0; i < emptyComponents; i++) {
            var type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.parse(buffer.readUtf()));
            if (type.isEmpty()) break; // If any component is unknown we have to stop parsing!
            builder.remove(type.get().value());
        }
        return builder.build();
    }

    /**
     * Writes the given item to the given buffer.
     */
    public static void writeItemStackToBuffer(RegistryFriendlyByteBuf buffer, ItemStack item) {
        if (item.isEmpty()) {
            buffer.writeVarInt(0);
        } else {
            buffer.writeVarInt(item.getCount());
            buffer.writeUtf(item.getItemHolder()
                    .unwrapKey()
                    .map(it -> it.location().toString())
                    .orElse("unknown"));

            var components = new ArrayList<Map.Entry<DataComponentType<?>, Optional<?>>>();
            var emptyComponents = new ArrayList<Map.Entry<DataComponentType<?>, Optional<?>>>();
            for (var entry : item.getComponentsPatch().entrySet()) {
                if (entry.getValue().isPresent()) {
                    components.add(entry);
                } else {
                    emptyComponents.add(entry);
                }
            }

            buffer.writeVarInt(components.size());
            buffer.writeVarInt(emptyComponents.size());
            for (var component : components) {
                buffer.writeUtf(BuiltInRegistries.DATA_COMPONENT_TYPE
                        .getKey(component.getKey())
                        .toString());
                encodeComponent(buffer, component.getKey(), component.getValue().get());
            }
            for (var empty : emptyComponents) {
                buffer.writeUtf(BuiltInRegistries.DATA_COMPONENT_TYPE
                        .getKey(empty.getKey())
                        .toString());
            }
        }
    }

    /**
     * Decodes a component from a buffer and adds it to the given builder.
     */
    public static <T> void decodeComponent(
            RegistryFriendlyByteBuf registryFriendlyByteBuf,
            DataComponentType<T> dataComponentType,
            DataComponentPatch.Builder builder) {
        builder.set(dataComponentType, dataComponentType.streamCodec().cast().decode(registryFriendlyByteBuf));
    }

    /**
     * Encodes a component into a buffer.
     */
    public static <T> void encodeComponent(
            RegistryFriendlyByteBuf registryFriendlyByteBuf, DataComponentType<T> dataComponentType, Object object) {
        dataComponentType.streamCodec().cast().encode(registryFriendlyByteBuf, (T) object);
    }

    private final ItemStack defaultValue;

    public ItemStackServerRule(int index) {
        this(index, ItemStack.EMPTY);
    }

    public ItemStackServerRule(int index, ItemStack defaultValue) {
        super(index);
        this.defaultValue = defaultValue;
        setValue(defaultValue);
    }

    @Override
    public ItemStack getDefault() {
        return defaultValue;
    }

    @Override
    public ItemStack read(RegistryFriendlyByteBuf buffer) {
        return readItemStackFromBuffer(buffer);
    }

    @Override
    public void write(ItemStack value, RegistryFriendlyByteBuf buffer) {
        writeItemStackToBuffer(buffer, value);
    }
}
