package com.noxcrew.noxesium.fabric.mixin.rules.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.fabric.registry.CommonEntityComponentTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Allows removal of the bubble particle around guardian beams to remove flickering.
 */
@Mixin(Guardian.class)
public class GuardianBubblesRuleMixin {

    @WrapOperation(
            method = "aiStep",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    public void spawnBubbleParticle(
            Level instance,
            ParticleOptions parameters,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ,
            Operation<Void> original) {
        var guardian = (Guardian) ((Object) this);
        if (!guardian.noxesium$hasComponent(CommonEntityComponentTypes.DISABLE_BUBBLES)) {
            original.call(instance, parameters, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}
