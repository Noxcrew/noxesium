package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

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
    public List<ItemStack> read(RegistryFriendlyByteBuf buffer) {
        return ItemStack.OPTIONAL_LIST_STREAM_CODEC.decode(buffer);
    }

    @Override
    public void write(List<ItemStack> value, RegistryFriendlyByteBuf buffer) {
        ItemStack.OPTIONAL_LIST_STREAM_CODEC.encode(buffer, value);
    }

    @Override
    protected void onValueChanged(List<ItemStack> oldValue, List<ItemStack> newValue) {
        super.onValueChanged(oldValue, newValue);
        hasChanged = true;
    }
}
