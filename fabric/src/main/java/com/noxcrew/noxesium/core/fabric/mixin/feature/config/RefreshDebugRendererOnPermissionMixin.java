package com.noxcrew.noxesium.core.fabric.mixin.feature.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.permissions.PermissionSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class RefreshDebugRendererOnPermissionMixin {
    @Inject(method = "setPermissions", at = @At("RETURN"))
    public void onSetPermissionLevel(PermissionSet permissions, CallbackInfo ci) {
        // Refresh whether the qib renderer is shown when you gain permissions
        Minecraft.getInstance().debugEntries.rebuildCurrentList();
    }
}
