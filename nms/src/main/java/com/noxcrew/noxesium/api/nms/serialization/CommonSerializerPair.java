package com.noxcrew.noxesium.api.nms.serialization;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the two types of codecs that works in the config or play phases.
 */
public record CommonSerializerPair<T>(Codec<T> codec, @Nullable StreamCodec<? super FriendlyByteBuf, T> streamCodec) {}
