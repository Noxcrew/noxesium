package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * A standard server rule that stores a boolean.
 */
public class BooleanServerRule extends ClientServerRule<Boolean> {

    private final boolean defaultValue;

    public BooleanServerRule(int index, boolean defaultValue) {
        super(index);
        this.defaultValue = defaultValue;
        setValue(defaultValue);
    }

    @Override
    public Boolean getDefault() {
        return defaultValue;
    }

    @Override
    public Boolean read(RegistryFriendlyByteBuf buffer) {
        return buffer.readBoolean();
    }

    @Override
    public void write(Boolean value, RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(value);
    }
}
