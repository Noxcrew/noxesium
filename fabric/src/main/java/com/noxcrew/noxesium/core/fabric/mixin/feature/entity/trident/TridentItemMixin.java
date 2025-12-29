package com.noxcrew.noxesium.core.fabric.mixin.feature.entity.trident;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundRiptidePacket;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
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

/**
 * Adds patches to the trident to make it entirely client-side.
 */
@Mixin(TridentItem.class)
public abstract class TridentItemMixin {

    @WrapOperation(
            method = "releaseUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    public boolean isInWaterOrRain(Player player, Operation<Boolean> original) {
        if (original.call(player)) return true;
        if (!GameComponents.getInstance()
                .noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_RIPTIDE_TRIDENTS)) return false;
        if (player != Minecraft.getInstance().player) return false;

        // Only for the local player do we check if they have coyote time currently!
        return player.noxesium$hasTridentCoyoteTime();
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    public boolean canStartChargingTrident(Player player, Operation<Boolean> original) {
        // If pre-charging is allowed we always allow you to start charging it.
        if (GameComponents.getInstance()
                        .noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_RIPTIDE_TRIDENTS)
                && GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.RIPTIDE_PRE_CHARGING))
            return true;
        return original.call(player);
    }

    @WrapOperation(
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
            Operation<Void> original,
            @Local(argsOnly = true) ItemStack itemStack,
            @Local(argsOnly = true) Level level,
            @Local(argsOnly = true) LivingEntity livingEntity,
            @Local(argsOnly = true) int i) {
        var player = Minecraft.getInstance().player;
        if (!GameComponents.getInstance()
                        .noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_RIPTIDE_TRIDENTS)
                || entity != player
                || player == null) {
            original.call(instance, ignored, entity, soundEvent, soundSource, volume, pitch);
            return;
        }

        // Play a sound locally to replace the remote sound
        instance.playLocalSound(entity, soundEvent, soundSource, volume, pitch);

        // Reset the coyote time as we've just activated the riptide.
        livingEntity.noxesium$resetTridentCoyoteTime();

        // Send the server a packet to inform it about the riptide as we may have used coyote time to trigger it!
        NoxesiumServerboundNetworking.send(new ServerboundRiptidePacket(
                player.getUsedItemHand() == InteractionHand.MAIN_HAND
                        ? player.getInventory().getSelectedSlot()
                        : Inventory.SLOT_OFFHAND));
    }
}
