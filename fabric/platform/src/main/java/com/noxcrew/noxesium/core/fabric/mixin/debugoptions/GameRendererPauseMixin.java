package com.noxcrew.noxesium.core.fabric.mixin.debugoptions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.client.DebugOption;
import com.noxcrew.noxesium.core.fabric.registry.CommonGameComponentTypes;
import net.minecraft.client.Minecraft;
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
        var restrictedOptions =
                Minecraft.getInstance().noxesium$getComponent(CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS);
        if (restrictedOptions != null && restrictedOptions.contains(DebugOption.PAUSE_ON_LOST_FOCUS.getKeyCode())) {
            return true;
        }
        return original.call(instance);
    }
}
