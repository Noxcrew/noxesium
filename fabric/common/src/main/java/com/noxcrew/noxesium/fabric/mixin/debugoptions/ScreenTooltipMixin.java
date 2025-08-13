package com.noxcrew.noxesium.fabric.mixin.debugoptions;

import com.noxcrew.noxesium.api.client.DebugOption;
import com.noxcrew.noxesium.fabric.registry.CommonGameComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Screen.class)
public class ScreenTooltipMixin {

    @Redirect(
            method = "getTooltipFromItem",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;advancedItemTooltips:Z"))
    private static boolean restrictAdvancedItemTooltips(net.minecraft.client.Options options) {
        var original = options.advancedItemTooltips;
        var restrictedOptions =
                Minecraft.getInstance().noxesium$getComponent(CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS);
        if (restrictedOptions != null && restrictedOptions.contains(DebugOption.ADVANCED_TOOLTIPS.getKeyCode())) {
            return false;
        }
        return original;
    }
}
