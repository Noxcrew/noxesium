package com.noxcrew.noxesium.feature.ui.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Wraps around a boss bar and updates it whenever the progress value of any bar changes.
 */
public class BossBarWrapper extends ElementWrapper {

    public BossBarWrapper() {
        registerVariable("progress", (minecraft, partialTicks) -> {
            var overlay = minecraft.gui.getBossOverlay();
            if (overlay.events.isEmpty()) return Map.of();

            var progress = new HashMap<>();
            for (var entry : overlay.events.entrySet()) {
                progress.put(entry.getKey(), entry.getValue().getProgress());
            }
            return progress;
        });
    }
}
