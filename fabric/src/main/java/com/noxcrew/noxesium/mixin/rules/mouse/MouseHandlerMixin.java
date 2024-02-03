package com.noxcrew.noxesium.mixin.rules.mouse;

import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.SmoothDouble;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    @Final
    private SmoothDouble smoothTurnX;

    @Shadow
    @Final
    private SmoothDouble smoothTurnY;

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    public void lockPlayerRotation(CallbackInfo ci) {
        // Cancel mouse movement during camera lock
        if (ServerRules.CAMERA_LOCKED.getValue()) {
            ci.cancel();

            // Reset smooth turning variables
            smoothTurnX.reset();
            smoothTurnY.reset();
        }
    }
}
