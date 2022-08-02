package com.noxcrew.noxesium.mixin.client;

import com.noxcrew.noxesium.NoxesiumMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Syncs up with the server whenever the GUI scale is updated.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "resizeDisplay", at = @At(value = "TAIL"))
    private void resizeDisplay(CallbackInfo ci) {
        NoxesiumMod.syncGuiScale();
    }
}
