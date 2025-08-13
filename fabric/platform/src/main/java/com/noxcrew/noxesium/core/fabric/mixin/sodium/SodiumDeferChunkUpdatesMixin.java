package com.noxcrew.noxesium.core.fabric.mixin.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.core.fabric.registry.CommonGameComponentTypes;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.client.Minecraft;
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
        if (Minecraft.getInstance().noxesium$hasComponent(CommonGameComponentTypes.DISABLE_DEFERRED_CHUNK_UPDATES)) {
            return true;
        }
        return original;
    }
}
