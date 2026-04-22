package com.noxcrew.noxesium.core.fabric.mixin.feature.zoom;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.core.fabric.feature.ZoomModule;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraZoomMixin {

    @WrapOperation(
            method = "calculateFov",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;modifyFovBasedOnDeathOrFluid(FF)F"))
    private float getCurrentFov(Camera instance, float partialTicks, float fov, Operation<Float> original) {
        var zoomModule = NoxesiumApi.getInstance().getFeatureOrNull(ZoomModule.class);
        if (zoomModule == null || !zoomModule.isRegistered()) return original.call(instance, partialTicks, fov);

        Double override = zoomModule.getFov(partialTicks);
        return original.call(instance, partialTicks, override == null ? fov : override.floatValue());
    }

    @ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
    private float applyZoomToFov(float original, float tickDelta) {
        var zoomModule = NoxesiumApi.getInstance().getFeatureOrNull(ZoomModule.class);
        if (zoomModule == null || !zoomModule.isRegistered()) return original;

        return original * zoomModule.getZoom(tickDelta);
    }

    @ModifyReturnValue(method = "calculateHudFov", at = @At("RETURN"))
    private float applyZoomToHudFov(float original, float tickDelta) {
        var zoomModule = NoxesiumApi.getInstance().getFeatureOrNull(ZoomModule.class);
        if (zoomModule == null || !zoomModule.isRegistered() || zoomModule.isKeepHandStationary()) return original;

        return original * zoomModule.getZoom(tickDelta);
    }
}
