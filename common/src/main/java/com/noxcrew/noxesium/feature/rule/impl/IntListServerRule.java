package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;
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
        int size = buffer.readVarInt();
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(buffer.readVarInt());
        }
        return list;
    }

    @Override
    public void write(List<Integer> value, RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(value.size());
        for (Integer integer : value) {
            buffer.writeVarInt(integer);
        }
    }

    public boolean contains(int value) {
        return getValue().contains(value);
    }

    public boolean add(int value) {
        List<Integer> currentValue = getValue();
        if (!currentValue.contains(value)) {
            currentValue.add(value);
            setValue(currentValue);
            return true;
        }
        return false;
    }

    public boolean remove(int value) {
        List<Integer> currentValue = getValue();
        boolean removed = currentValue.remove(Integer.valueOf(value));
        if (removed) {
            setValue(currentValue);
        }
        return removed;
    }

    public void clear() {
        setValue(new ArrayList<>());
    }

    public void setAll(List<Integer> values) {
        setValue(new ArrayList<>(values));
    }
}