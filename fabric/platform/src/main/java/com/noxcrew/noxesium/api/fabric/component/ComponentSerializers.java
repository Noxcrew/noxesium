package com.noxcrew.noxesium.api.fabric.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 * Stores codecs for serializing a component.
 */
public record ComponentSerializers<T>(
        Codec<T> codec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {}
