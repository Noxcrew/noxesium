package com.noxcrew.noxesium.mixin.debugoptions;

import com.noxcrew.noxesium.api.util.DebugOption;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiGraphics.class)
public class GuiGraphicsTooltipMixin {

    @Redirect(
            method = "renderComponentHoverEffect",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;advancedItemTooltips:Z"))
    private boolean restrictAdvancedItemTooltips(net.minecraft.client.Options options) {
        boolean original = options.advancedItemTooltips;

        if (ServerRules.RESTRICT_DEBUG_OPTIONS != null) {
            var restrictedOptions = ServerRules.RESTRICT_DEBUG_OPTIONS.getValue();
            if (restrictedOptions != null
                    && !restrictedOptions.isEmpty()
                    && restrictedOptions.contains(DebugOption.ADVANCED_TOOLTIPS.getKeyCode())) {
                return false;
            }
        }

        return original;
    }
}
