package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.network.FriendlyByteBuf;

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
    public String read(FriendlyByteBuf buffer) {
        return buffer.readUtf();
    }
}
