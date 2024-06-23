package com.noxcrew.noxesium.feature.ui.cache;

import com.noxcrew.noxesium.mixin.ui.render.ext.GuiExt;
import net.minecraft.util.Mth;

/**
 * Wraps around the title display and manages when it should be re-rendered.
 */
public class TitleWrapper extends ElementWrapper {

    public TitleWrapper() {
        registerVariable("alpha", (minecraft, deltaTracker) -> {
            var gui = minecraft.gui;
            var guiExt = (GuiExt) gui;
            var alpha = 255;
            var ticksElapsed = (float) guiExt.getTitleTime() - deltaTracker.getGameTimeDeltaPartialTick(false);

            if (guiExt.getTitleTime() > guiExt.getTitleFadeOutTime() + guiExt.getTitleStayTime()) {
                var fadeFactor = (float) (guiExt.getTitleFadeInTime() + guiExt.getTitleStayTime() + guiExt.getTitleFadeOutTime()) - ticksElapsed;
                alpha = (int) (fadeFactor * 255.0F / (float) guiExt.getTitleFadeInTime());
            }

            if (guiExt.getTitleTime() <= guiExt.getTitleFadeOutTime()) {
                alpha = (int) (ticksElapsed * 255.0F / (float) guiExt.getTitleFadeOutTime());
            }

            return Mth.clamp(alpha, 0, 255);
        });

        // Ensure we re-draw if the title time goes from 0 to not 0
        registerVariable("title visible", (minecraft, partialTicks) -> ((GuiExt) minecraft.gui).getTitleTime() >= 0);
    }
}
