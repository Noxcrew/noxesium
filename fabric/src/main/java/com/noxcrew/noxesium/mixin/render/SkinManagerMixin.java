package com.noxcrew.noxesium.mixin.render;

import com.mojang.authlib.GameProfile;
import com.noxcrew.noxesium.feature.render.cache.bossbar.BossBarCache;
import com.noxcrew.noxesium.feature.render.cache.tablist.TabListCache;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(SkinManager.class)
public class SkinManagerMixin {

    @Inject(method = "registerTextures", at = @At(value = "TAIL"))
    private void registerTexture(GameProfile gameProfile, SkinManager.TextureInfo textureInfo, CallbackInfoReturnable<CompletableFuture<PlayerSkin>> cir) {
        cir.getReturnValue().whenComplete((a, b) -> {
            // Whenever we finish loading a skin we make sure to update the tab list so we can show the actual skin!
            TabListCache.getInstance().clearCache();
        });
    }
}
