package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.render.CachedScoreboardContents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.SoundOptionsScreen;

import java.util.Objects;

/**
 * Updates the cached scoreboard whenever it changes.
 */
public class DisableScoreboardNumbersRule extends BooleanServerRule {

    public DisableScoreboardNumbersRule(int index) {
        super(index, false);
    }

    @Override
    protected void onValueChanged(Boolean oldValue, Boolean newValue) {
        CachedScoreboardContents.clearCache();
    }
}
