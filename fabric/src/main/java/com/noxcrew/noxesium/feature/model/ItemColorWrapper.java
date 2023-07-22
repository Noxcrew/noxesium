package com.noxcrew.noxesium.feature.model;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

/**
 * A wrapper around ItemColor which is used to cache the color of items
 * between quads. Sodium does cache looking up the ItemColor object between
 * different directions of the model but the individual color lookup is
 * still done once per quad. That's really bad when MCC Island uses giant
 * models with tons of quads that are all colored. It's well worth it to
 * sacrifice some memory to cache the lookups by tint, and since it's often
 * only colored with one tint index it should only be one lookup.
 * <p>
 * It's also separately optimized (for non-Sodium use cases) in looking up
 * just a leather item's color. This is also slow because it does a bunch
 * of extra unnecessary NBT safety checks.
 */
public class ItemColorWrapper implements ItemColor {

    private final ItemColor wrapped;
    private HashMap<Integer, Integer> cache;

    public ItemColorWrapper(ItemColor wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public int getColor(ItemStack itemStack, int tintIndex) {
        if (cache == null) {
            // Don't create the cache until this model needs color
            cache = new HashMap<>();
        } else {
            // Try to fetch the value from the cache, we only use these wrappers
            // when only one instance is created per item stack, so we don't need
            // to cache per item!
            var value = cache.get(tintIndex);
            if (value != null) {
                return value;
            }
        }
        var result = wrapped.getColor(itemStack, tintIndex);
        cache.put(tintIndex, result);
        return result;
    }
}
