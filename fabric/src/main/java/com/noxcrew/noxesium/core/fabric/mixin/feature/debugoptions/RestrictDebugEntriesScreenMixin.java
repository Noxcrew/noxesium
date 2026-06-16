package com.noxcrew.noxesium.core.fabric.mixin.feature.debugoptions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.core.fabric.feature.GameComponentHelper;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DebugOptionsScreen.OptionEntry.class)
public class RestrictDebugEntriesScreenMixin {
    @WrapOperation(
            method = "<init>",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntry;isAllowed(Z)Z"))
    public boolean isDebugEntryAllowed(
            DebugScreenEntry instance,
            boolean reducedDebugInfo,
            Operation<Boolean> original,
            @Local(argsOnly = true, name = "location") Identifier location) {
        return GameComponentHelper.isDebugEntryAllowed(location, original.call(instance, reducedDebugInfo));
    }
}
