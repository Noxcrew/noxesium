package com.noxcrew.noxesium.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.ui.render.api.NoxesiumRenderStateHolder;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Re-initializes the Noxesium packets when we enter the configuration phase.
 */
@Mixin(ServerConfigurationPacketListenerImpl.class)
public class ServerConfigurationMixin {

    @Inject(method = "startConfiguration", at = @At("TAIL"))
    public void startConfiguration(CallbackInfo ci) {
        // This mimics ClientConfigurationConnectionEvents.START on fabric.
        NoxesiumMod.getInstance().uninitialize();
        RenderSystem.recordRenderCall(() -> NoxesiumMod.forEachRenderStateHolder(NoxesiumRenderStateHolder::clear));
    }
}
