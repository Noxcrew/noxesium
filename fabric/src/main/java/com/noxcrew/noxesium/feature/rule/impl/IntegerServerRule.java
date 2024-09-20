package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.network.FriendlyByteBuf;

/**
 * A standard server rule that stores an integer.
 */
public class IntegerServerRule extends ClientServerRule<Integer> {

    private final int defaultValue;

    public IntegerServerRule(int index, int defaultValue) {
        super(index);
        this.defaultValue = defaultValue;
        setValue(defaultValue);
    }

    @Override
    public Integer getDefault() {
        return defaultValue;
    }

    @Override
    public Integer read(FriendlyByteBuf buffer) {
        return buffer.readVarInt();
    }

    @Override
    public void write(Integer value, FriendlyByteBuf buffer) {
        buffer.writeVarInt(value);
    }
}
