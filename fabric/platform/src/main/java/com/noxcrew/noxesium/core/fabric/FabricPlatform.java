package com.noxcrew.noxesium.core.fabric;

import com.noxcrew.noxesium.api.nms.NoxesiumPlatform;
import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
import net.kyori.adventure.text.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Implement platform-specific codecs for Fabric.
 */
public class FabricPlatform extends NoxesiumPlatform {
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Component> getComponentStreamCodec() {
        return ComponentSerialization.STREAM_CODEC.map(
                NonWrappingComponentSerializer.INSTANCE::deserialize,
                NonWrappingComponentSerializer.INSTANCE::serialize);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ItemStack> getItemStackStreamCodec() {
        return ItemStack.OPTIONAL_STREAM_CODEC;
    }
}
