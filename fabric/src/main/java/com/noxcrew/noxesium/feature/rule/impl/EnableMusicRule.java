package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.feature.rule.BooleanServerRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.SoundOptionsScreen;

import java.util.Objects;

/**
 * Updates the current state of playing music and the sound settings screen whenever
 * the setting is modified.
 */
public class EnableMusicRule extends BooleanServerRule {

    public EnableMusicRule(int index) {
        super(index, false);
    }

    @Override
    protected void onValueChanged(Boolean oldValue, Boolean newValue) {
        // If the sound options screen is open we need to close it as it may have changed
        var minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof SoundOptionsScreen) {
            minecraft.screen.onClose();
        }

        // If background music is playing and we've just enabled custom music, stop it!
        // This prevents all vanilla music from playing on servers with custom music.
        if (Objects.equals(newValue, true)) {
            minecraft.getMusicManager().stopPlaying();
        }
    }
}
