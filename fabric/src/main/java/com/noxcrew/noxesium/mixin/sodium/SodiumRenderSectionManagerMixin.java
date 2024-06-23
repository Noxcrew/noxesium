package com.noxcrew.noxesium.mixin.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.OverrideChunkUpdates;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(value = RenderSectionManager.class, remap = false)
public class SodiumRenderSectionManagerMixin {

    @ModifyReturnValue(method = "allowImportantRebuilds", at = @At("RETURN"))
    private static boolean overrideDeferChunkUpdates(boolean original) {
        if (NoxesiumMod.getInstance().getModule(OverrideChunkUpdates.class).shouldOverride()) {
            return true;
        }
        return original;
    }
}
