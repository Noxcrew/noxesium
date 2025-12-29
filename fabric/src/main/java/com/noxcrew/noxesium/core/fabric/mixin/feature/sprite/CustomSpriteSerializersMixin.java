package com.noxcrew.noxesium.core.fabric.mixin.feature.sprite;

import com.mojang.serialization.MapCodec;
import com.noxcrew.noxesium.core.fabric.feature.sprite.SkullSprite;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.network.chat.contents.objects.ObjectInfos;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Modifies the object component type to add support for custom sprite types.
 */
@Mixin(ObjectInfos.class)
public abstract class CustomSpriteSerializersMixin {

    @Shadow
    @Final
    private static ExtraCodecs.LateBoundIdMapper<String, MapCodec<? extends ObjectInfo>> ID_MAPPER;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void bootstrap(CallbackInfo ci) {
        ID_MAPPER.put("skull", SkullSprite.MAP_CODEC);
    }
}
