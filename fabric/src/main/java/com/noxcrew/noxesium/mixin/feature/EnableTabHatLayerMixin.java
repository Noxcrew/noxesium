package com.noxcrew.noxesium.mixin.feature;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * This replaces the call to [PlayerFaceRenderer] to use the default arguments
 * when rendering in the tab list (include hat, not upside down), this mirrors
 * every other usage of player head rendering (realms, spectator teleport) and
 * avoids issues where the hat layer disappears when a player leaves render distance.
 */
@Mixin(PlayerTabOverlay.class)
public abstract class EnableTabHatLayerMixin {

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;IIIZZ)V"), index = 5)
    private boolean alwaysRenderHat(boolean original) {
        return true;
    }
}
