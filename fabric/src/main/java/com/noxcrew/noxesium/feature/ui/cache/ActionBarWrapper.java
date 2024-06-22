package com.noxcrew.noxesium.feature.ui.cache;

import com.noxcrew.noxesium.mixin.ui.render.ext.GuiExt;
import net.minecraft.util.Mth;

/**
 * Wraps around the action bar and updates it whenever the fading animation changes.
 */
public class ActionBarWrapper extends ElementWrapper {

    public ActionBarWrapper() {
        registerVariable("alpha", (minecraft, deltaTracker) -> {
            var gui = minecraft.gui;
            var guiExt = (GuiExt) gui;
            var remainingTicks = (float) guiExt.getOverlayMessageTime() - deltaTracker.getGameTimeDeltaPartialTick(false);
            var alpha = (int) (remainingTicks * 255.0F / 20.0F);
            return Mth.clamp(alpha, 0, 255);
        });
    }
}
