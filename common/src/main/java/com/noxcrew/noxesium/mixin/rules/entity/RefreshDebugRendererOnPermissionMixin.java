package com.noxcrew.noxesium.mixin.rules.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class RefreshDebugRendererOnPermissionMixin {
    @Inject(method = "setPermissionLevel", at = @At("RETURN"))
    public void onSetPermissionLevel(int value, CallbackInfo ci) {
        // Refresh whether the qib renderer is shown when you gain permissions
        Minecraft.getInstance().debugEntries.rebuildCurrentList();
    }
}
