package com.noxcrew.noxesium.fabric.feature.rule.impl;

import com.noxcrew.noxesium.fabric.feature.rule.ClientServerRule;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

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
    public Optional<T> read(RegistryFriendlyByteBuf buffer) {
        return buffer.readOptional((buf) -> buf.readEnum(clazz));
    }

    @Override
    public void write(Optional<T> value, RegistryFriendlyByteBuf buffer) {
        buffer.writeOptional(value, FriendlyByteBuf::writeEnum);
    }

    @Override
    protected void onValueChanged(Optional<T> oldValue, Optional<T> newValue) {
        super.onValueChanged(oldValue, newValue);
        onChange.run();
    }
}
