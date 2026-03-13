package com.noxcrew.noxesium.core.fabric.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.core.fabric.feature.ZoomModule;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public class GameRendererZoomMixin {

    @Unique
    private boolean noxesium$lastFovSetting = false;

    @ModifyVariable(method = "getFov", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    private boolean getUseFovSetting(boolean baseValue) {
        // If we are using the zoom module we set the zoom manually and ignore the parameter!
        var zoomModule = NoxesiumApi.getInstance().getFeatureOrNull(ZoomModule.class);
        noxesium$lastFovSetting = baseValue;
        if (zoomModule == null || !zoomModule.isRegistered() || zoomModule.getFov(0f) == null) return baseValue;
        return false;
    }

    @ModifyVariable(method = "getFov", at = @At(value = "STORE"), ordinal = 1)
    private float getCurrentFov(float baseValue, @Local(argsOnly = true) float tickDelta) {
        var zoomModule = NoxesiumApi.getInstance().getFeatureOrNull(ZoomModule.class);
        if (zoomModule == null || !zoomModule.isRegistered()) return baseValue;

        // Don't override anything if we're not using the actual player FOV and only
        // setting the rendering perspective matrix.
        if (!noxesium$lastFovSetting) return baseValue;

        Double fov = zoomModule.getFov(tickDelta);
        return fov == null ? baseValue : fov.floatValue();
    }

    @ModifyReturnValue(method = "getFov(Lnet/minecraft/client/Camera;FZ)F", at = @At("RETURN"))
    private float applyZoomToFov(float original, Camera camera, float tickDelta, boolean useFovSetting) {
        var zoomModule = NoxesiumApi.getInstance().getFeatureOrNull(ZoomModule.class);
        if (zoomModule == null || !zoomModule.isRegistered()) return original;

        return original * zoomModule.getZoom(tickDelta);
    }

    @ModifyExpressionValue(
            method = "renderLevel",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/GameRenderer;getFov(Lnet/minecraft/client/Camera;FZ)F",
                            ordinal = 1))
    private float keepHandStationary(float fov, @Local(ordinal = 0) float tickDelta) {
        var zoomModule = NoxesiumApi.getInstance().getFeatureOrNull(ZoomModule.class);
        if (zoomModule == null || !zoomModule.isRegistered()) return fov;

        if (zoomModule.isKeepHandStationary()) {
            return fov / zoomModule.getZoom(tickDelta);
        }

        return fov;
    }
}
