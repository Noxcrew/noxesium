package com.noxcrew.noxesium.mixin.inventory;

import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds patches to the trident to make it entirely client-side.
 */
@Mixin(TridentItem.class)
public abstract class TridentItemMixin {

    @Shadow
    public abstract int getUseDuration(ItemStack itemStack, LivingEntity livingEntity);

    @Shadow
    private static boolean isTooDamagedToUse(ItemStack itemStack) {
        return false;
    }

    @Inject(method = "releaseUsing", at = @At(value = "HEAD"))
    public void coyoteTimeRelease(ItemStack itemStack, Level level, LivingEntity livingEntity, int remaining, CallbackInfo ci) {
        if (!ServerRules.ENABLE_SMOOTHER_CLIENT_TRIDENT.getValue()) return;

        if (livingEntity instanceof Player player) {
            // Ignore non-riptide tridents!
            var riptide = EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player);
            if (riptide <= 0f || isTooDamagedToUse(itemStack)) return;

            // Ignore if we didn't meet the charge condition!
            var duration = getUseDuration(itemStack, livingEntity) - remaining;
            if (duration < 10) return;

            // Ignore if we're already in water (riptide triggered)
            if (player.isInWaterOrRain()) return;

            // Activate coyote time!
            livingEntity.noxesium$triggerTridentCoyoteTime();
        }
    }

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    public void playSound(Level instance, Player player, Entity entity, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        if (!ServerRules.ENABLE_SMOOTHER_CLIENT_TRIDENT.getValue()) return;

        // Reset the coyote time as we've just activated the riptide.
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.noxesium$resetTridentCoyoteTime();
        }

        instance.playLocalSound(
            Minecraft.getInstance().player,
            soundEvent,
            soundSource,
            volume,
            pitch
        );
    }
}
