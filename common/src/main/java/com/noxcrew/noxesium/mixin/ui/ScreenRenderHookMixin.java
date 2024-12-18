package com.noxcrew.noxesium.mixin.ui;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.ui.render.screen.ScreenRenderingHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces rendering of the currently opened inventory UI (or screen) with a buffered renderer.
 */
@Mixin(GameRenderer.class)
public class ScreenRenderHookMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    public void renderScreen(Screen instance, GuiGraphics guiGraphics, int width, int height, float deltaTime) {
        if ((instance instanceof MenuAccess || instance instanceof ChatScreen) && NoxesiumMod.getInstance().getConfig().enableUiLimiting) {
            // Create a new state object and let it render
            if (ScreenRenderingHolder.getInstance().render(guiGraphics, width, height, deltaTime, instance)) return;
        } else {
            // Destroy the state if it exists
            ScreenRenderingHolder.getInstance().clear();
        }

        // Directly draw everything to the screen
        instance.renderWithTooltip(guiGraphics, width, height, deltaTime);
    }
}
