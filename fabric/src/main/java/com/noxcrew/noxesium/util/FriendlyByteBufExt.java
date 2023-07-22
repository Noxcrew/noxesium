package com.noxcrew.noxesium.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Offers extension functions to the Friendly Byte Buffer.
 */
public class FriendlyByteBufExt {

    /**
     * Writes the given item stack to this buffer in a format that can be deciphered regardless
     * of server version. Usually ViaVersion can be relied upon to do such a conversion but
     * that is not applicable to custom packets.
     */
    public static FriendlyByteBuf writeUniversalItem(FriendlyByteBuf buffer, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);

            Item item = itemStack.getItem();

            // The only real difference from the base version is sending the
            // item key instead of the id. This is usually not necessary as
            // the registries are synced but necessary when sending across versions.
            buffer.writeUtf(BuiltInRegistries.ITEM.getKey(item).toString());
            buffer.writeByte(itemStack.getCount());
            CompoundTag compoundTag = null;
            if (item.canBeDepleted() || item.shouldOverrideMultiplayerNbt()) {
                compoundTag = itemStack.getTag();
            }

            buffer.writeNbt(compoundTag);
        }
        return buffer;
    }
}
