package com.noxcrew.noxesium.mixin.rules.qib;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.feature.entity.LivingEntityExtension;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks into the living entity code and implements [LivingEntityExtension].
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityExtensionMixin implements LivingEntityExtension {

    @Shadow
    protected int autoSpinAttackTicks;

    @Shadow
    public abstract boolean canBeAffected(MobEffectInstance effect);

    @Shadow
    public abstract AttributeMap getAttributes();

    @Unique
    private int noxesium$coyoteTime = 0;

    @Unique
    private boolean noxesium$soundQueued = false;

    @Unique
    private Map<Holder<MobEffect>, MobEffectInstance> noxesium$activeEffects = new IdentityHashMap<>();

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

    @Override
    public void noxesium$clearClientsidePotionEffects() {
        var effects = new HashMap<>(noxesium$activeEffects);
        noxesium$activeEffects.clear();
        for (var entry : effects.keySet()) {
            noxesium$onEffectRemoved(entry);
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

            // Perform client ticking
            mobeffectinstance.blendState.tick(mobeffectinstance);

            // Perform server ticking (effects need to be client-only anyway)
            if (!mobeffectinstance.tickServer(
                    null, (LivingEntity) (Object) this, () -> noxesium$onEffectUpdated(mobeffectinstance))) {
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
    public boolean noxesium$hasTridentCoyoteTime() {
        return noxesium$coyoteTime > 0;
    }

    @Override
    public void noxesium$resetTridentCoyoteTime() {
        noxesium$coyoteTime = 0;
    }

    /**
     * Replace the entity flag with the local value so we don't rely on the server.
     */
    @Inject(method = "isAutoSpinAttack", at = @At(value = "HEAD"), cancellable = true)
    private void isAutoSpinAttack(CallbackInfoReturnable<Boolean> cir) {
        if (!ServerRules.ENABLE_SMOOTHER_CLIENT_TRIDENT.getValue()) return;
        if (((Object) this) != Minecraft.getInstance().player) return;
        cir.setReturnValue(this.autoSpinAttackTicks > 0);
    }

    /**
     * Add an indicator noise when you can release a trident.
     */
    @Inject(method = "baseTick", at = @At(value = "TAIL"))
    private void onTick(CallbackInfo ci) {
        if (!ServerRules.ENABLE_SMOOTHER_CLIENT_TRIDENT.getValue()) return;

        var entity = (LivingEntity) ((Object) this);
        if (entity instanceof Player player) {
            if (player != Minecraft.getInstance().player) return;

            // Update coyote time
            if (player.isInWaterOrRain()) {
                noxesium$coyoteTime = ServerRules.RIPTIDE_COYOTE_TIME.getValue();
            } else if (noxesium$coyoteTime > 0) {
                noxesium$coyoteTime--;
            }

            // Determine if you are charging a riptide!
            var useItem = entity.getUseItem();
            if (useItem.isEmpty() || useItem.getItem() != Items.TRIDENT) {
                noxesium$soundQueued = false;
                return;
            }

            // Only check riptide tridents!
            var spinAttack = EnchantmentHelper.getTridentSpinAttackStrength(useItem, player);
            if (spinAttack <= 0f || noxesium$isTooDamagedToUse(useItem)) {
                noxesium$soundQueued = false;
                return;
            }

            // Check if there's been 9 ticks of charging exactly
            // since you can use it when we reach 10.
            var remaining = entity.getUseItemRemainingTicks();
            var duration = useItem.getUseDuration(entity) - remaining;
            if (duration == 9 || noxesium$soundQueued) {
                if (player.isInWaterOrRain()) {
                    player.level()
                            .playLocalSound(
                                    player,
                                    SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                                            "noxesium", "trident.ready_indicator")),
                                    SoundSource.PLAYERS,
                                    1f,
                                    1f);
                } else {
                    noxesium$soundQueued = true;
                    return;
                }
            }
            noxesium$soundQueued = false;
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
