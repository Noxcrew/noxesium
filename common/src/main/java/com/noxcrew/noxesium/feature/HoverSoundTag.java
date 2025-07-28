package com.noxcrew.noxesium.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record HoverSoundTag(
        Optional<ResourceLocation> hoverOn,
        Optional<ResourceLocation> hoverOff,
        Boolean onlyPlayInNonPlayerInventories
) {
    public static ItemStack hoveredItemStack = null;

    public static Codec<HoverSoundTag> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("hover_on").forGetter(HoverSoundTag::hoverOn),
            ResourceLocation.CODEC.optionalFieldOf("hover_off").forGetter(HoverSoundTag::hoverOff),
            Codec.BOOL.optionalFieldOf("only_play_in_non_player_inventories", false).forGetter(HoverSoundTag::onlyPlayInNonPlayerInventories)
    ).apply(instance, HoverSoundTag::new));
}
