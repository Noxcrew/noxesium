package com.noxcrew.noxesium.mixin.entity;

import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.feature.entity.LivingEntityExtension;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Hooks into the living entity code and implements [LivingEntityExtension].
 */
@SuppressWarnings("UnreachableCode")
@Mixin(LivingEntity.class)
public abstract class LivingEntityExtensionMixin implements LivingEntityExtension {

    @Shadow
    protected int autoSpinAttackTicks;

    @Shadow
    public abstract boolean canBeAffected(MobEffectInstance effect);

    @Shadow
    public abstract AttributeMap getAttributes();

    @Unique
    private int noxesium$attemptUseTicks = 0;

    @Unique
    private Map<Holder<MobEffect>, MobEffectInstance> noxesium$activeEffects = Maps.newHashMap();

    @Override
    public void noxesium$addClientsidePotionEffect(MobEffectInstance instance) {
        if (canBeAffected(instance)) {
            var mobeffectinstance = noxesium$activeEffects.get(instance.getEffect());
            if (mobeffectinstance == null) {
                noxesium$activeEffects.put(instance.getEffect(), instance);
                noxesium$onEffectAdded(instance);
            } else if (mobeffectinstance.update(instance)) {
                noxesium$onEffectUpdated(mobeffectinstance);
            }
        }
    }

    @Override
    public void noxesium$removeClientsidePotionEffect(Holder<MobEffect> effect) {
        if (noxesium$activeEffects.remove(effect) != null) {
            noxesium$onEffectRemoved(effect);
        }
    }

    @Override
    public void noxesium$updateClientsidePotionEffects() {
        for (var instance : noxesium$activeEffects.values()) {
            instance.getEffect().value().addAttributeModifiers(getAttributes(), instance.getAmplifier());
        }
    }

    @Unique
    private void noxesium$onEffectAdded(MobEffectInstance instance) {
        instance.getEffect().value().addAttributeModifiers(getAttributes(), instance.getAmplifier());
    }

    @Unique
    private void noxesium$onEffectUpdated(MobEffectInstance instance) {
        var mobeffect = instance.getEffect().value();
        mobeffect.removeAttributeModifiers(getAttributes());
        mobeffect.addAttributeModifiers(getAttributes(), instance.getAmplifier());
    }

    @Unique
    private void noxesium$onEffectRemoved(Holder<MobEffect> instance) {
        instance.value().removeAttributeModifiers(getAttributes());
    }

    @Inject(method = "tickEffects", at = @At(value = "HEAD"))
    private void tickEffects(CallbackInfo ci) {
        var iterator = noxesium$activeEffects.keySet().iterator();
        while (iterator.hasNext()) {
            var holder = iterator.next();
            var mobeffectinstance = noxesium$activeEffects.get(holder);

            if (!mobeffectinstance.tick((LivingEntity) (Object) this, () -> noxesium$onEffectUpdated(mobeffectinstance))) {
                iterator.remove();
                noxesium$onEffectRemoved(holder);
            }
        }
    }

    @Inject(method = "hasEffect", at = @At(value = "HEAD"), cancellable = true)
    public void hasEffect(Holder<MobEffect> holder, CallbackInfoReturnable<Boolean> cir) {
        if (noxesium$activeEffects.containsKey(holder)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getEffect", at = @At(value = "HEAD"), cancellable = true)
    public void getEffect(Holder<MobEffect> holder, CallbackInfoReturnable<MobEffectInstance> cir) {
        if (noxesium$activeEffects.containsKey(holder)) {
            cir.setReturnValue(noxesium$activeEffects.get(holder));
        }
    }

    @WrapMethod(method = "getActiveEffects")
    public Collection<MobEffectInstance> getActiveEffects(Operation<Collection<MobEffectInstance>> original) {
        if (!noxesium$activeEffects.isEmpty()) {
            var combined = new ArrayList<>(noxesium$activeEffects.values());
            combined.addAll(original.call());
            return combined;
        }
        return original.call();
    }

    @Override
    public void noxesium$triggerTridentCoyoteTime() {
        noxesium$attemptUseTicks = 5;
    }

    @Override
    public void noxesium$resetTridentCoyoteTime() {
        noxesium$attemptUseTicks = 0;
    }

    /**
     * Replace the entity flag with the local value so we don't rely on the server.
     */
    @Inject(method = "isAutoSpinAttack", at = @At(value = "HEAD"), cancellable = true)
    private void isAutoSpinAttack(CallbackInfoReturnable<Boolean> cir) {
        if (!ServerRules.ENABLE_SMOOTHER_CLIENT_TRIDENT.getValue()) return;
        cir.setReturnValue(this.autoSpinAttackTicks > 0);
    }

    /**
     * Add an indicator noise when you can release a trident.
     */
    @Inject(method = "baseTick", at = @At(value = "TAIL"))
    private void onTick(CallbackInfo ci) {
        if (!ServerRules.ENABLE_SMOOTHER_CLIENT_TRIDENT.getValue()) return;

        // Reduce the use attempt ticks
        if (noxesium$attemptUseTicks > 0) noxesium$attemptUseTicks--;

        var entity = (LivingEntity) ((Object) this);
        if (entity instanceof Player player) {
            // Then we try to activate the riptide if there's coyote time!
            // This basically makes it so if riptiding becomes possible in the 2
            // ticks after the player releases the button it still goes off.
            // Try to find the item they would've been using!
            var useItem = entity.getUseItem();
            var remaining = entity.getUseItemRemainingTicks();
            if (useItem.isEmpty()) {
                useItem = entity.getMainHandItem();
            }
            if (useItem.isEmpty()) return;
            if (useItem.getItem() != Items.TRIDENT) return;

            // Only check riptide tridents!
            var spinAttack = EnchantmentHelper.getTridentSpinAttackStrength(useItem, player);
            if (spinAttack <= 0f || noxesium$isTooDamagedToUse(useItem)) return;

            // Check if the player is in the water now!
            if (noxesium$attemptUseTicks > 0 && player.isInWaterOrRain()) {
                // Copied from TridentItem.java
                var holder = EnchantmentHelper.pickHighestLevel(useItem, EnchantmentEffectComponents.TRIDENT_SOUND).orElse(SoundEvents.TRIDENT_THROW);
                player.awardStat(Stats.ITEM_USED.get(useItem.getItem()));

                float g = player.getYRot();
                float h = player.getXRot();
                float k = -Mth.sin(g * 0.017453292F) * Mth.cos(h * 0.017453292F);
                float l = -Mth.sin(h * 0.017453292F);
                float m = Mth.cos(g * 0.017453292F) * Mth.cos(h * 0.017453292F);
                float n = Mth.sqrt(k * k + l * l + m * m);
                k *= spinAttack / n;
                l *= spinAttack / n;
                m *= spinAttack / n;
                player.push((double) k, (double) l, (double) m);
                player.startAutoSpinAttack(20, 8.0F, useItem);
                if (player.onGround()) {
                    float o = 1.1999999F;
                    player.move(MoverType.SELF, new Vec3(0.0, 1.1999999284744263, 0.0));
                }

                player.level().playLocalSound(
                    player,
                    holder.value(),
                    SoundSource.PLAYERS,
                    1f,
                    1f
                );

                noxesium$resetTridentCoyoteTime();
                return;
            }

            // Check if there's been 9 ticks of charging exactly
            // since you can use it when we reach 10.
            var duration = useItem.getUseDuration(entity) - remaining;
            if (duration == 9) {
                player.level().playLocalSound(
                    player,
                    SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("noxesium", "trident.ready_indicator")),
                    SoundSource.PLAYERS,
                    1f,
                    1f
                );
            }
        }
    }

    /**
     * Copied from TridentItem.java.
     */
    @Unique
    private static boolean noxesium$isTooDamagedToUse(ItemStack item) {
        return item.getDamageValue() >= item.getMaxDamage() - 1;
    }
}
