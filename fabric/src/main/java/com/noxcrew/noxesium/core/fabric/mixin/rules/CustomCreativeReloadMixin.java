package com.noxcrew.noxesium.core.fabric.mixin.rules;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Mixes in to the creative tab system to ensure they update whenever the creative items
 * server rule updates!
 */
@Mixin(CreativeModeTab.ItemDisplayParameters.class)
public class CustomCreativeReloadMixin {

    @ModifyReturnValue(method = "needsUpdate", at = @At("RETURN"))
    public boolean needsUpdate(boolean original) {
        if (original) return true;
        if (NoxesiumMod.getInstance().hasCreativeTabChanged) {
            NoxesiumMod.getInstance().hasCreativeTabChanged = false;
            return true;
        }
        return false;
    }
}
