package com.noxcrew.noxesium.mixin.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.DeferMode;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adds override for disabling the deferred chunk updates setting in Sodium.
 */
@Pseudo
@Mixin(value = RenderSectionManager.class, remap = false)
public class SodiumDeferChunkUpdatesMixin {

    @WrapOperation(
            method = "createTerrainRenderList",
            at =
                    @At(
                            value = "FIELD",
                            target =
                                    "Lnet/caffeinemc/mods/sodium/client/gui/SodiumGameOptions$PerformanceSettings;chunkBuildDeferMode:Lnet/caffeinemc/mods/sodium/client/render/chunk/DeferMode;"))
    private static DeferMode overrideDeferChunkUpdates(
            SodiumGameOptions.PerformanceSettings instance, Operation<DeferMode> original) {
        if (ServerRules.DISABLE_DEFERRED_CHUNK_UPDATES.getValue()) {
            return DeferMode.ZERO_FRAMES;
        }
        return original.call(instance);
    }
}
