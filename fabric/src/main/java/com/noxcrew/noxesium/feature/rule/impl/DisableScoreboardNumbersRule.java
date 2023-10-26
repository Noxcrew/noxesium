package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.render.cache.ScoreboardCache;

/**
 * Updates the cached scoreboard whenever it changes.
 */
public class DisableScoreboardNumbersRule extends BooleanServerRule {

    public DisableScoreboardNumbersRule(int index) {
        super(index, false);
    }

    @Override
    protected void onValueChanged(Boolean oldValue, Boolean newValue) {
        ScoreboardCache.getInstance().clearCache();
    }
}
