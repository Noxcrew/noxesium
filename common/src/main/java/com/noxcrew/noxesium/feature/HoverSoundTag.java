package com.noxcrew.noxesium.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record HoverSoundTag(
        Optional<Sound> hoverOn,
        Optional<Sound> hoverOff,
        Boolean onlyPlayInNonPlayerInventories
) {

    public record Sound(ResourceLocation sound, float volume, float pitchMin, float pitchMax) {
        public static Codec<Sound> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                ResourceLocation.CODEC.fieldOf("sound").forGetter(Sound::sound),
                Codec.FLOAT.optionalFieldOf("volume", 1f).forGetter(Sound::volume),
                Codec.FLOAT.optionalFieldOf("pitch_min", 1f).forGetter(Sound::pitchMin),
                Codec.FLOAT.optionalFieldOf("pitch_max", 1f).forGetter(Sound::pitchMax)
        ).apply(instance, Sound::new));
    }

    public static Codec<HoverSoundTag> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Sound.CODEC.optionalFieldOf("hover_on").forGetter(HoverSoundTag::hoverOn),
            Sound.CODEC.optionalFieldOf("hover_off").forGetter(HoverSoundTag::hoverOff),
            Codec.BOOL.optionalFieldOf("only_play_in_non_player_inventories", false).forGetter(HoverSoundTag::onlyPlayInNonPlayerInventories)
    ).apply(instance, HoverSoundTag::new));
}
