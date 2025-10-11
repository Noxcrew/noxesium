package com.noxcrew.noxesium.mixin.rules.entity;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.entity.SpatialDebuggingRenderer;
import java.util.List;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Shadow
    @Final
    private List<DebugRenderer.SimpleDebugRenderer> opaqueRenderers;

    @Unique
    private DebugRenderer.SimpleDebugRenderer noxesium$spatialDebugRenderer = new SpatialDebuggingRenderer();

    @Inject(method = "refreshRendererList", at = @At("RETURN"))
    public void render(CallbackInfo ci) {
        if (NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging) {
            opaqueRenderers.add(noxesium$spatialDebugRenderer);
        }
    }
}
