package com.noxcrew.noxesium.core.fabric.mixin.rules.mouse;

import com.noxcrew.noxesium.api.registry.GameComponents;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.SmoothDouble;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseLockMixin {

    @Shadow
    @Final
    private SmoothDouble smoothTurnX;

    @Shadow
    @Final
    private SmoothDouble smoothTurnY;

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    public void lockPlayerRotation(CallbackInfo ci) {
        // Cancel mouse movement during camera lock
        if (GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.CAMERA_LOCKED)) {
            ci.cancel();

            // Reset smooth turning variables
            smoothTurnX.reset();
            smoothTurnY.reset();
        }
    }
}
