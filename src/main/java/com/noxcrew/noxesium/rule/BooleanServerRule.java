package com.noxcrew.noxesium.rule;

import net.minecraft.network.FriendlyByteBuf;

/**
 * A standard server rule that stores a boolean.
 */
public class BooleanServerRule extends ServerRule<Boolean> {

    private final boolean defaultValue;

    public BooleanServerRule(int index, boolean defaultValue) {
        super(index);
        this.defaultValue = defaultValue;
    }

    @Override
    public Boolean getDefault() {
        return defaultValue;
    }

    @Override
    public Boolean read(FriendlyByteBuf buffer) {
        return buffer.readBoolean();
    }
}
