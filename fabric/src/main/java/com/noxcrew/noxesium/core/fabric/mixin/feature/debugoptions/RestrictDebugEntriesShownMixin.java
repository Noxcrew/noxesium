package com.noxcrew.noxesium.core.fabric.mixin.feature.debugoptions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.core.fabric.feature.GameComponentHelper;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DebugScreenEntryList.class)
public class RestrictDebugEntriesShownMixin {
    @WrapOperation(
            method = "lambda$rebuildCurrentList$0",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntry;isAllowed(Z)Z"))
    public boolean isDebugEntryAllowed(
            DebugScreenEntry instance,
            boolean reducedDebugInfo,
            Operation<Boolean> original,
            @Local(argsOnly = true, name = "key") Identifier key) {
        return GameComponentHelper.isDebugEntryAllowed(key, original.call(instance, reducedDebugInfo));
    }
}
