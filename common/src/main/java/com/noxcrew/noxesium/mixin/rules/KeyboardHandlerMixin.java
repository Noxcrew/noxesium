package com.noxcrew.noxesium.mixin.rules;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import com.noxcrew.noxesium.feature.rule.impl.IntListServerRule;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @WrapOperation(
            method = "handleDebugKeys",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyboardHandler;debugFeedbackTranslated(Ljava/lang/String;[Ljava/lang/Object;)V",
                    ordinal = 0
            )
    )
    private void beforeDebugKeysHandle(KeyboardHandler instance, String key, Object[] objects, Operation<Void> original) {
        IntListServerRule restrictedDebugOptions = ServerRules.RESTRICT_DEBUG_OPTIONS;
        int keyCode = (int) objects[0];

        if (!restrictedDebugOptions.contains(keyCode)) {
            original.call(instance, key, objects);
        }
    }

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void onHandleDebugKeys(int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (ServerRules.RESTRICT_DEBUG_OPTIONS.contains(keyCode)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "handleChunkDebugKeys", at = @At("HEAD"), cancellable = true)
    private void onHandleChunkDebugKeys(int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (ServerRules.RESTRICT_DEBUG_OPTIONS.contains(keyCode)) {
            cir.setReturnValue(true);
        }
    }
}