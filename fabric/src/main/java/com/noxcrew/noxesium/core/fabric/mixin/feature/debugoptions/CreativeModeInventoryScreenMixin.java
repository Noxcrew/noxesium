package com.noxcrew.noxesium.core.fabric.mixin.feature.debugoptions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.core.feature.DebugOption;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin {

    @WrapOperation(
            method = "getTooltipFromContainerItem",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;advancedItemTooltips:Z"))
    private boolean restrictAdvancedItemTooltips(Options instance, Operation<Boolean> original) {
        var restrictedOptions =
                GameComponents.getInstance().noxesium$getComponent(CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS);
        if (restrictedOptions != null && restrictedOptions.contains(DebugOption.ADVANCED_TOOLTIPS.getKeyCode())) {
            return false;
        }
        return original.call(instance);
    }
}
