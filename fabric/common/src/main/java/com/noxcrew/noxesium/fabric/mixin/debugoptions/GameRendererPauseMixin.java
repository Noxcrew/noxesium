package com.noxcrew.noxesium.fabric.mixin.debugoptions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.client.DebugOption;
import com.noxcrew.noxesium.fabric.feature.rule.ServerRules;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererPauseMixin {

    // Uses WrapOperation because Axiom already redirects the base field!
    @WrapOperation(
            method = "render",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;pauseOnLostFocus:Z"))
    private boolean restrictPauseOnLostFocus(Options instance, Operation<Boolean> original) {
        if (ServerRules.RESTRICT_DEBUG_OPTIONS != null) {
            var restrictedOptions = ServerRules.RESTRICT_DEBUG_OPTIONS.getValue();
            if (restrictedOptions != null
                    && !restrictedOptions.isEmpty()
                    && restrictedOptions.contains(DebugOption.PAUSE_ON_LOST_FOCUS.getKeyCode())) {
                return true;
            }
        }

        return original.call(instance);
    }
}
