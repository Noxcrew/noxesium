package com.noxcrew.noxesium.mixin.debug;

import com.noxcrew.noxesium.api.util.DebugOption;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.client.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin {

    @Unique
    private static final Logger noxesium$LOGGER = LoggerFactory.getLogger("Noxesium/OptionsMixin");

    @Shadow
    public boolean advancedItemTooltips;

    @Shadow
    public boolean pauseOnLostFocus;

    @Inject(method = "processOptions", at = @At("TAIL"))
    private void resetRestrictedOptionsAfterLoading(CallbackInfo ci) {
        noxesium$forceRestrictOptions();
    }

    @Unique
    public void noxesium$forceRestrictOptions() {
        noxesium$LOGGER.debug("Forcing restriction check, advancedItemTooltips before: {}", advancedItemTooltips);

        if (ServerRules.RESTRICT_DEBUG_OPTIONS != null) {
            var restrictedOptions = ServerRules.RESTRICT_DEBUG_OPTIONS.getValue();
            if (restrictedOptions != null && !restrictedOptions.isEmpty()) {
                if (advancedItemTooltips && restrictedOptions.contains(DebugOption.ADVANCED_TOOLTIPS.getKeyCode())) {
                    noxesium$LOGGER.debug("Setting advancedItemTooltips to false");
                    advancedItemTooltips = false;
                }

                if (!pauseOnLostFocus && restrictedOptions.contains(DebugOption.PAUSE_ON_LOST_FOCUS.getKeyCode())) {
                    pauseOnLostFocus = true;
                }
            }
        }

        noxesium$LOGGER.debug("After restriction check, advancedItemTooltips: {}", advancedItemTooltips);
    }
}