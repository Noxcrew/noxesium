package com.noxcrew.noxesium.api.nms.serialization;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the two types of codecs.
 */
public record SerializerPair<T>(
        Codec<T> codec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {}
