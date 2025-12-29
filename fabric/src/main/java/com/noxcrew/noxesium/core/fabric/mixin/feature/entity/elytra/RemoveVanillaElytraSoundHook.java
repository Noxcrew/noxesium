package com.noxcrew.noxesium.core.fabric.mixin.feature.entity.elytra;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Removes vanilla's sound played while gliding with the elytra when
 * using the custom one.
 */
@Mixin(LocalPlayer.class)
public class RemoveVanillaElytraSoundHook {
    @Redirect(
            method = "onSyncedDataUpdated",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isFallFlying()Z"))
    public boolean onCheckIfGlidingForSound(LocalPlayer instance) {
        if (!GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_ELYTRA))
            return instance.isFallFlying();
        return false;
    }
}
