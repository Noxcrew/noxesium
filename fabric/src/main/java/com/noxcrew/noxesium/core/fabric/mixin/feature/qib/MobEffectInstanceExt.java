package com.noxcrew.noxesium.core.fabric.mixin.feature.qib;

import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobEffectInstance.class)
public interface MobEffectInstanceExt {
    @Accessor("blendState")
    MobEffectInstance.BlendState getBlendState();
}
