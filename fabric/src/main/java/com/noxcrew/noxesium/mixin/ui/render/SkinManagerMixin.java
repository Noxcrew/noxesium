package com.noxcrew.noxesium.mixin.ui.render;

import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.noxcrew.noxesium.feature.ui.wrapper.ElementManager;
import com.noxcrew.noxesium.feature.ui.wrapper.TabListWrapper;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Listens to skins being loaded and re-loads the tab list when it happens.
 */
@Mixin(SkinManager.class)
public abstract class SkinManagerMixin {

    @Inject(method = "registerTextures", at = @At("TAIL"))
    private void clearTabSkinCache(UUID uUID, MinecraftProfileTextures minecraftProfileTextures, CallbackInfoReturnable<CompletableFuture<PlayerSkin>> cir) {
        cir.getReturnValue().whenComplete((a, b) -> {
            // Whenever we finish loading a skin we make sure to update the tab list so we can show the actual skin!
            ElementManager.getInstance(TabListWrapper.class).requestRedraw();
        });
    }
}
