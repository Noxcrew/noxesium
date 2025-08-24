package com.noxcrew.noxesium.api.nms.component;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.nms.serialization.ComponentSerializerRegistry;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

/**
 * Provides extension methods for fetching and modifying the components
 * on item stacks. These are unsafe but generic methods that work without
 * needing to know anything about the implementation of the item stack.
 * <p>
 * If the item stack is a static object consider using interface injection
 * instead and caching the de-serialized data.
 */
public class NoxesiumComponentHelper {
    /**
     * Returns the given component data on this holder. These values are not cached and will
     * be re-serialized on every fetch. Please cache the result of this function whenever
     * possible.
     */
    @Nullable
    public static <T> T getNoxesiumComponent(
            NoxesiumRegistry<NoxesiumComponentType<?>> registry,
            DataComponentHolder componentHolder,
            NoxesiumComponentType<T> type) {
        var codec = ComponentSerializerRegistry.getSerializers(registry, type);
        if (codec == null) return null;
        var customData = componentHolder.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;
        var noxesiumData = customData.getUnsafe().getCompound(NoxesiumReferences.COMPONENT_NAMESPACE);
        if (noxesiumData.isEmpty()) return null;
        var value = noxesiumData.get().get(type.id().asString());
        if (value == null) return null;
        var result = codec.codec().decode(NbtOps.INSTANCE, value);
        if (result.hasResultOrPartial()) {
            var partial = result.resultOrPartial();
            if (partial.isPresent()) {
                return partial.get().getFirst();
            }
        }
        return null;
    }

    /**
     * Returns whether this holder has the given component set.
     */
    public static <T> boolean hasNoxesiumComponent(DataComponentHolder componentHolder, NoxesiumComponentType<T> type) {
        var customData = componentHolder.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;
        var noxesiumData = customData.getUnsafe().getCompound(NoxesiumReferences.COMPONENT_NAMESPACE);
        return noxesiumData
                .map(compoundTag -> compoundTag.contains(type.id().asString()))
                .orElse(false);
    }

    /**
     * Sets the given component on this holder.
     */
    public static <T> void setNoxesiumComponent(
            NoxesiumRegistry<NoxesiumComponentType<?>> registry,
            ItemStack componentHolder,
            NoxesiumComponentType<T> type,
            @Nullable T value) {
        var codec = ComponentSerializerRegistry.getSerializers(registry, type);
        if (codec == null) return;
        if (value == null) {
            var customData = componentHolder.get(DataComponents.CUSTOM_DATA);
            if (customData == null) return;
            var noxesiumDataOptional = customData.getUnsafe().getCompound(NoxesiumReferences.COMPONENT_NAMESPACE);
            if (noxesiumDataOptional.isEmpty()) return;
            if (!noxesiumDataOptional.get().contains(type.id().asString())) return;

            var tag = customData.getUnsafe().copy();
            var noxesiumData =
                    tag.getCompound(NoxesiumReferences.COMPONENT_NAMESPACE).get();
            noxesiumData.remove(type.id().asString());
            if (noxesiumData.isEmpty()) {
                tag.remove(NoxesiumReferences.COMPONENT_NAMESPACE);
                if (tag.isEmpty()) {
                    componentHolder.remove(DataComponents.CUSTOM_DATA);
                    return;
                }
            } else {
                tag.put(NoxesiumReferences.COMPONENT_NAMESPACE, noxesiumData);
            }
            componentHolder.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            return;
        }

        var result = codec.codec().encodeStart(NbtOps.INSTANCE, value);
        if (result.hasResultOrPartial()) {
            var encoded = result.resultOrPartial().orElse(null);
            if (encoded == null) return;
            var customData = componentHolder.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            var tag = customData.getUnsafe().copy();
            var noxesiumData = tag.getCompoundOrEmpty(NoxesiumReferences.COMPONENT_NAMESPACE);
            noxesiumData.put(type.id().asString(), encoded);
            tag.put(NoxesiumReferences.COMPONENT_NAMESPACE, noxesiumData);
            componentHolder.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
