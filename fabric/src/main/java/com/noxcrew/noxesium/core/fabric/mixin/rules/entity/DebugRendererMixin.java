package com.noxcrew.noxesium.core.fabric.mixin.rules.entity;

import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.fabric.feature.entity.SpatialDebuggingRenderer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.server.permissions.Permissions;
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
    private List<DebugRenderer.SimpleDebugRenderer> renderers;

    @Unique
    private final DebugRenderer.SimpleDebugRenderer noxesium$spatialDebugRenderer = new SpatialDebuggingRenderer();

    @Inject(method = "refreshRendererList", at = @At("RETURN"))
    public void render(CallbackInfo ci) {
        if (NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging
                && Minecraft.getInstance().player != null
                && Minecraft.getInstance().player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            renderers.add(noxesium$spatialDebugRenderer);
        }
    }
}
