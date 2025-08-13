package com.noxcrew.noxesium.fabric.feature.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

/**
 * Allows customising whether a slot can be hovered over and which sprites to draw when donig so.
 */
public record Hoverable(
        boolean hoverable, Optional<ResourceLocation> frontSprite, Optional<ResourceLocation> backSprite) {

    public static Codec<Hoverable> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    Codec.BOOL.optionalFieldOf("hoverable", true).forGetter(Hoverable::hoverable),
                    ResourceLocation.CODEC.optionalFieldOf("front_sprite").forGetter(Hoverable::frontSprite),
                    ResourceLocation.CODEC.optionalFieldOf("back_sprite").forGetter(Hoverable::backSprite))
            .apply(instance, Hoverable::new));
}
