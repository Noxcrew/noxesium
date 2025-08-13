package com.noxcrew.noxesium.fabric.mixin.rules.trident;

import com.noxcrew.noxesium.fabric.network.serverbound.ServerboundRiptidePacket;
import com.noxcrew.noxesium.fabric.registry.CommonGameComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

    @Redirect(
            method = "releaseUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    public boolean isInWaterOrRain(Player player) {
        if (player.isInWaterOrRain()) return true;
        if (!Minecraft.getInstance().noxesium$hasComponent(CommonGameComponentTypes.ENABLE_SMOOTHER_CLIENT_TRIDENT)) return false;
        if (player != Minecraft.getInstance().player) return false;

        // Only for the local player do we check if they have coyote time currently!
        return player.noxesium$hasTridentCoyoteTime();
    }

    @Redirect(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    public boolean canStartChargingTrident(Player player) {
        // If pre-charging is allowed we always allow you to start charging it.
        if (Minecraft.getInstance().noxesium$hasComponent(CommonGameComponentTypes.RIPTIDE_PRE_CHARGING)) return true;
        return player.isInWaterOrRain();
    }

    @Redirect(
            method = "releaseUsing",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    public void playSound(
            Level instance,
            Entity ignored,
            Entity entity,
            SoundEvent soundEvent,
            SoundSource soundSource,
            float volume,
            float pitch,
            ItemStack itemStack,
            Level level,
            LivingEntity livingEntity,
            int i) {
        var player = Minecraft.getInstance().player;
        if (!Minecraft.getInstance().noxesium$hasComponent(CommonGameComponentTypes.ENABLE_SMOOTHER_CLIENT_TRIDENT) || entity != player || player == null) {
            instance.playSound(ignored, entity, soundEvent, soundSource, volume, pitch);
            return;
        }

        // Play a sound locally to replace the remote sound
        instance.playLocalSound(entity, soundEvent, soundSource, volume, pitch);

        // Reset the coyote time as we've just activated the riptide.
        livingEntity.noxesium$resetTridentCoyoteTime();

        // Send the server a packet to inform it about the riptide as we may have used coyote time to trigger it!
        new ServerboundRiptidePacket(
                        player.getUsedItemHand() == InteractionHand.MAIN_HAND
                                ? player.getInventory().getSelectedSlot()
                                : Inventory.SLOT_OFFHAND)
                .send();
    }
}
