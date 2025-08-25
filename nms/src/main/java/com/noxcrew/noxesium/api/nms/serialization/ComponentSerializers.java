package com.noxcrew.noxesium.api.nms.serialization;

import com.noxcrew.noxesium.api.component.NoxesiumComponentListener;
import org.jetbrains.annotations.Nullable;

/**
 * Stores codecs for serializing a component.
 */
public record ComponentSerializers<T>(
        SerializerPair<T> serializers, @Nullable NoxesiumComponentListener<T, ?> listener) {}
