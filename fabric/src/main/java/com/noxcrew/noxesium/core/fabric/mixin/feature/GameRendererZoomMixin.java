package com.noxcrew.noxesium.core.fabric.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.core.fabric.feature.ZoomModule;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererZoomMixin {

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
