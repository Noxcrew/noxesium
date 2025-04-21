package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A server rule that stores a list of integers.
 */
public class IntListServerRule extends ClientServerRule<List<Integer>> {

    private final List<Integer> defaultValue;

    public IntListServerRule(int index) {
        this(index, Collections.emptyList());
    }

    public IntListServerRule(int index, List<Integer> defaultValue) {
        super(index);
        this.defaultValue = new ArrayList<>(defaultValue);
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
}