package com.noxcrew.noxesium.core.fabric.mixin.feature.entity.windcharge;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.core.fabric.util.ExplosionPrediction;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WindCharge.class)
public abstract class WindChargeMixin {

    @Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/util/random/WeightedList;Lnet/minecraft/core/Holder;)V"))
    public void redirectExplosion(Level instance, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, double x, double y, double z, float radius, boolean fire, Level.ExplosionInteraction explosionInteraction, ParticleOptions particleOptions, ParticleOptions particleOptions2, WeightedList<ExplosionParticleInfo> explosionParticleInfoWeightedList, Holder<SoundEvent> soundEventHolder) {
        if (GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_WINDCHARGES)) {
            Explosion.BlockInteraction blockInteraction = Explosion.BlockInteraction.TRIGGER_BLOCK;
            Vec3 vec3 = new Vec3(x, y, z);
            ExplosionPrediction explosion = new ExplosionPrediction(instance, entity, damageSource, explosionDamageCalculator, vec3, radius, blockInteraction);
            int i = explosion.explode();

            instance.playLocalSound(
                    x,
                    y,
                    z,
                    soundEventHolder.value(),
                    SoundSource.BLOCKS,
                    4.0F,
                    (1.0F + (instance.random.nextFloat() - instance.random.nextFloat()) * 0.2F) * 0.7F,
                    false
            );
            instance.addParticle(particleOptions, x, y, z, 1.0, 0.0, 0.0);
            ((ClientLevel) instance).trackExplosionEffects(new Vec3(x, y, z), radius, i, explosionParticleInfoWeightedList);
        } else {
            instance.explode(entity, damageSource, explosionDamageCalculator, x, y, z, radius, fire, explosionInteraction, particleOptions, particleOptions2, explosionParticleInfoWeightedList, soundEventHolder);
        }
    }

}
