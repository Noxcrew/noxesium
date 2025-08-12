package com.noxcrew.noxesium.fabric.feature.rule.impl;

import com.noxcrew.noxesium.fabric.feature.rule.ClientServerRule;
import java.awt.Color;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;

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
    public Optional<Color> read(RegistryFriendlyByteBuf buffer) {
        return buffer.readOptional((buf) -> new Color(buf.readVarInt(), true));
    }

    @Override
    public void write(Optional<Color> value, RegistryFriendlyByteBuf buffer) {
        buffer.writeOptional(value, (buf, color) -> buf.writeVarInt(color.getRGB()));
    }
}
