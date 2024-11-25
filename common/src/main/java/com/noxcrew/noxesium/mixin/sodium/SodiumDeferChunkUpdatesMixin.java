package com.noxcrew.noxesium.mixin.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.feature.rule.ServerRules;
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

    @ModifyReturnValue(method = "allowImportantRebuilds", at = @At("RETURN"))
    private static boolean overrideDeferChunkUpdates(boolean original) {
        if (ServerRules.DISABLE_DEFERRED_CHUNK_UPDATES.getValue()) {
            return true;
        }
        return original;
    }
}
