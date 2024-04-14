package com.noxcrew.noxesium.mixin.general;

import com.noxcrew.noxesium.NoxesiumMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes resetting of toggle keys on respawn configurable in the accessibility settings.
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Redirect(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;resetToggleKeys()V"))
    public void onResetToggleKeys() {
        if (NoxesiumMod.getInstance().getConfig().resetToggleKeys) {
            KeyMapping.resetToggleKeys();
        }
    }
}
