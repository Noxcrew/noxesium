package com.noxcrew.noxesium.api.nms;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Provides NMS platform-specific implementations for defining the codecs for items and text.
 */
public abstract class NoxesiumPlatform {
    protected static NoxesiumPlatform instance;

    /**
     * Returns the singleton instance of this class.
     */
    public static NoxesiumPlatform getInstance() {
        Preconditions.checkNotNull(instance, "Cannot get platform instance before it is defined");
        return instance;
    }

    /**
     * Sets the platform instance.
     */
    public static void setInstance(NoxesiumPlatform instance) {
        Preconditions.checkState(NoxesiumPlatform.instance == null, "Cannot set the platform instance twice!");
        NoxesiumPlatform.instance = instance;
    }

    /**
     * Returns the stream codec for components.
     */
    public abstract StreamCodec<RegistryFriendlyByteBuf, Component> getComponentStreamCodec();

    /**
     * Returns the stream codec for item stacks.
     */
    public abstract StreamCodec<RegistryFriendlyByteBuf, ItemStack> getItemStackStreamCodec();
}
