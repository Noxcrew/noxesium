package com.noxcrew.noxesium.core.fabric.mixin.debugoptions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.client.DebugOption;
import com.noxcrew.noxesium.core.fabric.registry.CommonGameComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Screen.class)
public class ScreenTooltipMixin {

    @WrapOperation(
            method = "getTooltipFromItem",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;advancedItemTooltips:Z"))
    private static boolean restrictAdvancedItemTooltips(Options instance, Operation<Boolean> original) {
        var restrictedOptions =
                Minecraft.getInstance().noxesium$getComponent(CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS);
        if (restrictedOptions != null && restrictedOptions.contains(DebugOption.ADVANCED_TOOLTIPS.getKeyCode())) {
            return false;
        }
        return original.call(instance);
    }
}
