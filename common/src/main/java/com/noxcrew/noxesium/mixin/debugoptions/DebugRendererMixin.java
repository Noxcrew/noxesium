package com.noxcrew.noxesium.mixin.debugoptions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.util.DebugOption;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import java.util.Objects;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @WrapOperation(
            method = "refreshRendererList",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;isCurrentlyEnabled(Lnet/minecraft/resources/ResourceLocation;)Z"))
    private boolean restrictChunkBorderRendering(
            DebugScreenEntryList instance, ResourceLocation id, Operation<Boolean> original) {
        if (!original.call(instance, id)) return false;
        return !Objects.equals(id, DebugScreenEntries.CHUNK_BORDERS)
                || ServerRules.RESTRICT_DEBUG_OPTIONS == null
                || !ServerRules.RESTRICT_DEBUG_OPTIONS.getValue().contains(DebugOption.CHUNK_BOUNDARIES.getKeyCode());
    }
}
