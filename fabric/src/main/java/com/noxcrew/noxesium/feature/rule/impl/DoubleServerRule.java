package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.network.FriendlyByteBuf;

/**
 * A standard server rule that stores a double.
 */
public class DoubleServerRule extends ClientServerRule<Double> {

    private final double defaultValue;

    public DoubleServerRule(int index, double defaultValue) {
        super(index);
        this.defaultValue = defaultValue;
        setValue(defaultValue);
    }

    @Override
    public Double getDefault() {
        return defaultValue;
    }

    @Override
    public Double read(FriendlyByteBuf buffer) {
        return buffer.readDouble();
    }
}
