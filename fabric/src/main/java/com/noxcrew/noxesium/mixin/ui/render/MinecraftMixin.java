package com.noxcrew.noxesium.mixin.ui.render;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.TeamGlowHotkeys;
import com.noxcrew.noxesium.feature.ui.wrapper.ElementWrapper;
import com.noxcrew.noxesium.feature.ui.wrapper.ElementManager;
import com.noxcrew.noxesium.feature.ui.wrapper.ChatWrapper;
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
        ElementManager.getAllWrappers().forEach(ElementWrapper::requestRedraw);
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void refreshChat(Screen newScreen, CallbackInfo ci) {
        if (newScreen instanceof ChatScreen || this.screen instanceof ChatScreen) {
            ElementManager.getInstance(ChatWrapper.class).requestRedraw();
        }
    }

    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean checkIfToggledTeamGlowing(boolean original, Entity entity) {
        if (original) return true;
        return entity.getTeam() != null &&
                // Only allow using the glowing outlines when flying is allowed == they are spectating
                Minecraft.getInstance().player.getAbilities().mayfly &&
                // Check that the team color is in the glowing teams list
                NoxesiumMod.getInstance().getModule(TeamGlowHotkeys.class).getGlowingTeams().contains(entity.getTeam().getColor());
    }
}
