package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * A standard server rule that stores a boolean.
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
    public ItemStack read(FriendlyByteBuf buffer) {
        return buffer.readJsonWithCodec(ItemStack.CODEC);
    }
}
