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
    private final Runnable onChange;

    public OptionalEnumServerRule(int index, Class<T> clazz, Optional<T> defaultValue, Runnable onChange) {
        super(index);
        this.clazz = clazz;
        this.defaultValue = defaultValue;
        this.onChange = onChange;
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

    @Override
    public void write(Optional<T> value, FriendlyByteBuf buffer) {
        if (value.isPresent()) {
            buffer.writeBoolean(true);
            buffer.writeEnum(value.get());
        } else {
            buffer.writeBoolean(false);
        }
    }

    @Override
    protected void onValueChanged(Optional<T> oldValue, Optional<T> newValue) {
        super.onValueChanged(oldValue, newValue);
        onChange.run();
    }
}
