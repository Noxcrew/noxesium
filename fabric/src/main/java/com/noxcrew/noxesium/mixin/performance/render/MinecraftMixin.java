package com.noxcrew.noxesium.mixin.performance.render;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.island.MccIslandTracker;
import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.cache.chat.ChatCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Syncs up with the server whenever the GUI scale is updated.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    @Nullable
    public Screen screen;

    @Inject(method = "resizeDisplay", at = @At("TAIL"))
    private void refreshElements(CallbackInfo ci) {
        ElementCache.getAllCaches().forEach(ElementCache::clearCache);
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void refreshChat(Screen newScreen, CallbackInfo ci) {
        if (newScreen instanceof ChatScreen || this.screen instanceof ChatScreen) {
            ChatCache.getInstance().clearCache();
        }
    }

    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean checkIfToggledTeamGlowing(boolean original, Entity entity) {
        if (original) return true;
        return entity.getTeam() != null && NoxesiumMod.getInstance().getModule(MccIslandTracker.class).getGlowingTeams().contains(entity.getTeam().getName());
    }
}
