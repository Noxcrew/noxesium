package com.noxcrew.noxesium.mixin.settings;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.noxcrew.noxesium.NoxesiumMod;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Makes resetting of toggle keys on respawn configurable in the accessibility settings.
 */
@Mixin(LocalPlayer.class)
public abstract class ResetToggleKeyMixin {

    @WrapWithCondition(
            method = "respawn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;resetToggleKeys()V"))
    public boolean onResetToggleKeys() {
        return NoxesiumMod.getInstance().getConfig().resetToggleKeys;
    }
}
