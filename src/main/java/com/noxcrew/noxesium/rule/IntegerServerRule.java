package com.noxcrew.noxesium.rule;

import net.minecraft.network.FriendlyByteBuf;

/**
 * A standard server rule that stores an integer.
 */
public class IntegerServerRule extends ServerRule<Integer> {

    private final int defaultValue;

    public IntegerServerRule(int index, int defaultValue) {
        super(index);
        this.defaultValue = defaultValue;
    }

    @Override
    public Integer getDefault() {
        return defaultValue;
    }

    @Override
    public Integer read(FriendlyByteBuf buffer) {
        return buffer.readVarInt();
    }
}
