package com.noxcrew.noxesium.fabric.feature.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

/**
 * Defines a sound to play when hovering over an item stack.
 */
public record HoverSound(Optional<Sound> hoverOn, Optional<Sound> hoverOff, boolean onlyPlayInNonPlayerInventories) {

    public record Sound(Holder<SoundEvent> sound, float volume, float pitchMin, float pitchMax) {
        public static Codec<Sound> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                        SoundEvent.CODEC.fieldOf("sound").forGetter(Sound::sound),
                        Codec.FLOAT.optionalFieldOf("volume", 1f).forGetter(Sound::volume),
                        Codec.FLOAT.optionalFieldOf("pitch_min", 1f).forGetter(Sound::pitchMin),
                        Codec.FLOAT.optionalFieldOf("pitch_max", 1f).forGetter(Sound::pitchMax))
                .apply(instance, Sound::new));
    }

    public static Codec<HoverSound> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    Sound.CODEC.optionalFieldOf("hover_on").forGetter(HoverSound::hoverOn),
                    Sound.CODEC.optionalFieldOf("hover_off").forGetter(HoverSound::hoverOff),
                    Codec.BOOL
                            .optionalFieldOf("only_play_in_non_player_inventories", false)
                            .forGetter(HoverSound::onlyPlayInNonPlayerInventories))
            .apply(instance, HoverSound::new));
}
