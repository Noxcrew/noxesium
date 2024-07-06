package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A standard server rule that stores a list of items.
 */
public class ItemStackListServerRule extends ClientServerRule<List<ItemStack>> {

    private final List<ItemStack> defaultValue;
    private boolean hasChanged = false;

    public ItemStackListServerRule(int index) {
        this(index, List.of());
    }

    public ItemStackListServerRule(int index, List<ItemStack> defaultValue) {
        super(index);
        this.defaultValue = defaultValue;
        setValue(defaultValue);
    }

    /**
     * Returns whether this value recently changed once. Sets the value back to
     * false when this method is called.
     */
    public boolean hasChangedRecently() {
        if (hasChanged) {
            hasChanged = false;
            return true;
        }
        return false;
    }

    @Override
    public List<ItemStack> getDefault() {
        return defaultValue;
    }

    @Override
    public List<ItemStack> read(FriendlyByteBuf buffer) {
        var amount = buffer.readVarInt();
        var array = new ArrayList<ItemStack>(amount);
        for (int i = 0; i < amount; i++) {
            array.add(buffer.readJsonWithCodec(ItemStack.CODEC));
        }
        return array;
    }

    @Override
    protected void onValueChanged(List<ItemStack> oldValue, List<ItemStack> newValue) {
        super.onValueChanged(oldValue, newValue);
        hasChanged = true;
    }
}
