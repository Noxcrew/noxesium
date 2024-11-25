package com.noxcrew.noxesium.mixin.rules;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.feature.rule.ServerRules;
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
        return original || ServerRules.CUSTOM_CREATIVE_ITEMS.hasChangedRecently();
    }
}
