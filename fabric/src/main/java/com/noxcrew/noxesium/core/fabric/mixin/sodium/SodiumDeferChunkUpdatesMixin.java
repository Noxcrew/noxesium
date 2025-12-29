package com.noxcrew.noxesium.core.fabric.mixin.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.caffeinemc.mods.sodium.client.gui.SodiumOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.DeferMode;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import org.objectweb.asm.Opcodes;
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
                                    "Lnet/caffeinemc/mods/sodium/client/gui/SodiumOptions$PerformanceSettings;chunkBuildDeferMode:Lnet/caffeinemc/mods/sodium/client/render/chunk/DeferMode;",
                            opcode = Opcodes.GETFIELD))
    private static DeferMode overrideDeferChunkUpdates(
            SodiumOptions.PerformanceSettings instance, Operation<DeferMode> original) {
        if (GameComponents.getInstance()
                .noxesium$hasComponent(CommonGameComponentTypes.DISABLE_DEFERRED_CHUNK_UPDATES)) {
            return DeferMode.ZERO_FRAMES;
        }
        return original.call(instance);
    }
}
