package com.noxcrew.noxesium.fabric.mixin.debugoptions;

import com.noxcrew.noxesium.api.client.DebugOption;
import com.noxcrew.noxesium.fabric.registry.CommonGameComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin {

    @Redirect(
            method = "getTooltipFromContainerItem",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;advancedItemTooltips:Z"))
    private boolean restrictAdvancedItemTooltips(net.minecraft.client.Options options) {
        boolean original = options.advancedItemTooltips;
        var optional = Minecraft.getInstance().noxesium$getOptionalComponent(CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS);
        if (optional.isPresent()) {
            var restrictedOptions = optional.get();
            if (restrictedOptions.contains(DebugOption.ADVANCED_TOOLTIPS.getKeyCode())) {
                return false;
            }
        }
        return original;
    }
}
