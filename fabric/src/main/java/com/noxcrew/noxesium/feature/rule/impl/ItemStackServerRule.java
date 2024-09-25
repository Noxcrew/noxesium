package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * A standard server rule that stores an item stack.
 */
public class ItemStackServerRule extends ClientServerRule<ItemStack> {

    private final ItemStack defaultValue;

    public ItemStackServerRule(int index) {
        this(index, ItemStack.EMPTY);
    }

    public ItemStackServerRule(int index, ItemStack defaultValue) {
        super(index);
        this.defaultValue = defaultValue;
        setValue(defaultValue);
    }

    @Override
    public ItemStack getDefault() {
        return defaultValue;
    }

    @Override
    public ItemStack read(RegistryFriendlyByteBuf buffer) {
        return ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
    }

    @Override
    public void write(ItemStack value, RegistryFriendlyByteBuf buffer) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, value);
    }
}
