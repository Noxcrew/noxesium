package com.noxcrew.noxesium.fabric.feature.rule.impl;

import com.noxcrew.noxesium.fabric.feature.rule.ClientServerRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * A server rule that stores a list of integers.
 */
public class IntListServerRule extends ClientServerRule<List<Integer>> {

    private final List<Integer> defaultValue;
    private final Runnable onChange;

    public IntListServerRule(int index) {
        this(index, Collections.emptyList());
    }

    public IntListServerRule(int index, List<Integer> defaultValue) {
        this(index, defaultValue, () -> {});
    }

    public IntListServerRule(int index, List<Integer> defaultValue, Runnable onChange) {
        super(index);
        this.defaultValue = new ArrayList<>(defaultValue);
        this.onChange = onChange;
        setValue(new ArrayList<>(defaultValue));
    }

    @Override
    public List<Integer> getDefault() {
        return new ArrayList<>(defaultValue);
    }

    @Override
    public List<Integer> read(RegistryFriendlyByteBuf buffer) {
        return buffer.readList(FriendlyByteBuf::readVarInt);
    }

    @Override
    public void write(List<Integer> value, RegistryFriendlyByteBuf buffer) {
        buffer.writeCollection(value, FriendlyByteBuf::writeVarInt);
    }

    @Override
    protected void onValueChanged(List<Integer> oldValue, List<Integer> newValue) {
        super.onValueChanged(oldValue, newValue);
        onChange.run();
    }
}
