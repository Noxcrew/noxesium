package com.noxcrew.noxesium.mixin.debugoptions;

import com.noxcrew.noxesium.api.util.DebugOption;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class GameRendererPauseMixin {

    @Redirect(
            method = "render",
            at = @At(
                value = "FIELD",
                target = "Lnet/minecraft/client/Options;pauseOnLostFocus:Z"
            )
    )
    private boolean restrictPauseOnLostFocus(net.minecraft.client.Options options) {
        boolean original = options.pauseOnLostFocus;
        
        if (ServerRules.RESTRICT_DEBUG_OPTIONS != null) {
            var restrictedOptions = ServerRules.RESTRICT_DEBUG_OPTIONS.getValue();
            if (restrictedOptions != null && !restrictedOptions.isEmpty() &&
                restrictedOptions.contains(DebugOption.PAUSE_ON_LOST_FOCUS.getKeyCode())) {
                return true;
            }
        }
        
        return original;
    }
}