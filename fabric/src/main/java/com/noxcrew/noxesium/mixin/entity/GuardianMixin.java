package com.noxcrew.noxesium.mixin.entity;

import com.noxcrew.noxesium.feature.entity.ExtraEntityData;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Allows removal of the bubble particle around guardian beams to remove flickering.
 */
@Mixin(Guardian.class)
public class GuardianMixin {

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    public void spawnBubbleParticle(Level instance, ParticleOptions parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        var guardian = (Guardian) ((Object) this);
        if (!guardian.getExtraData(ExtraEntityData.DISABLE_BUBBLES)) {
            instance.addParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}
