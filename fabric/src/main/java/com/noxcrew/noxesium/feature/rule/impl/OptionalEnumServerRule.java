package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;

/**
 * A standard server rule that stores an optional enum.
 */
public class OptionalEnumServerRule<T extends Enum<T>> extends ClientServerRule<Optional<T>> {

    private final Optional<T> defaultValue;
    private final Class<T> clazz;

    public OptionalEnumServerRule(int index, Class<T> clazz, Optional<T> defaultValue) {
        super(index);
        this.clazz = clazz;
        this.defaultValue = defaultValue;
        setValue(defaultValue);
    }

    @Override
    public Optional<T> getDefault() {
        return defaultValue;
    }

    @Override
    public Optional<T> read(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            return Optional.of(buffer.readEnum(clazz));
        }
        return Optional.empty();
    }
}
