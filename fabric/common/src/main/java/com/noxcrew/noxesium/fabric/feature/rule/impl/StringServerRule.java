package com.noxcrew.noxesium.fabric.feature.rule.impl;

import com.noxcrew.noxesium.fabric.feature.rule.ClientServerRule;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * A standard server rule that stores a string.
 */
public class StringServerRule extends ClientServerRule<String> {

    private final String defaultValue;

    public StringServerRule(int index, String defaultValue) {
        super(index);
        this.defaultValue = defaultValue;
        setValue(defaultValue);
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }

    @Override
    public String read(RegistryFriendlyByteBuf buffer) {
        return buffer.readUtf();
    }

    @Override
    public void write(String value, RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(value);
    }
}
