package com.noxcrew.noxesium.mixin.inventory;

import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Adds patches to the trident to make it entirely client-side.
 */
@Mixin(TridentItem.class)
public abstract class TridentItemMixin {

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    public boolean isInWaterOrRain(Player player) {
        if (player.isInWaterOrRain()) return true;
        if (!ServerRules.ENABLE_SMOOTHER_CLIENT_TRIDENT.getValue()) return false;
        if (player != Minecraft.getInstance().player) return false;

        // Only for the local player do we check if they have coyote time currently!
        return player.noxesium$hasTridentCoyoteTime();
    }

    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    public void playSound(Level instance, Player player, Entity entity, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        if (!ServerRules.ENABLE_SMOOTHER_CLIENT_TRIDENT.getValue()) return;
        if (entity != Minecraft.getInstance().player) return;

        // Reset the coyote time as we've just activated the riptide.
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.noxesium$resetTridentCoyoteTime();
        }

        instance.playLocalSound(
                entity,
                soundEvent,
                soundSource,
                volume,
                pitch
        );
    }
}
