package com.noxcrew.noxesium.mixin.rules;

import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void onHandleDebugKeys(int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (ServerRules.RESTRICT_DEBUG_OPTIONS != null && ServerRules.RESTRICT_DEBUG_OPTIONS.getValue().contains(keyCode)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "handleChunkDebugKeys", at = @At("HEAD"), cancellable = true)
    private void onHandleChunkDebugKeys(int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (ServerRules.RESTRICT_DEBUG_OPTIONS != null && ServerRules.RESTRICT_DEBUG_OPTIONS.getValue().contains(keyCode)) {
            cir.setReturnValue(true);
        }
    }
}