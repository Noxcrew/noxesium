package com.noxcrew.noxesium.mixin;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * This replaces the call to [PlayerFaceRenderer] to use the default arguments
 * when rendering in the tab list (include hat, not upside down), this mirrors
 * every other usage of player head rendering (realms, spectator teleport) and
 * avoids issues where the hat layer disappears when a player leaves render distance.
 */
@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;IIIZZ)V"))
    private void modifyArguments(Args args) {
        args.set(5, true);
    }
}
