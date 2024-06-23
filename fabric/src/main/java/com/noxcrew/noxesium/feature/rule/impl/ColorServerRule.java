package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.network.FriendlyByteBuf;

import java.awt.Color;
import java.util.Optional;

/**
 * A standard server rule that stores an optional color.
 */
public class ColorServerRule extends ClientServerRule<Optional<Color>> {

    private final Optional<Color> defaultValue;

    public ColorServerRule(int index, Optional<Color> defaultValue) {
        super(index);
        this.defaultValue = defaultValue;
        setValue(defaultValue);
    }

    @Override
    public Optional<Color> getDefault() {
        return defaultValue;
    }

    @Override
    public Optional<Color> read(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            var rgba = buffer.readVarInt();
            return Optional.of(new Color(rgba, true));
        }
        return Optional.empty();
    }
}
