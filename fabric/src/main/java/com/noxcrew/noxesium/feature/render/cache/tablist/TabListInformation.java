package com.noxcrew.noxesium.feature.render.cache.tablist;

import com.noxcrew.noxesium.feature.render.cache.ElementInformation;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stores information about the state of the tab list.
 */
public record TabListInformation(
        List<BakedComponent> header,
        List<BakedComponent> footer,
        List<PlayerInfo> players,
        List<UUID> blinking,
        Map<UUID, BakedComponent> names,
        int columnWidth,
        int maxNameWidth,
        int maxScoreWidth,
        int width,
        int left,
        int playersPerColumn,
        boolean showSkins,
        @Nullable Objective objective
) implements ElementInformation {

    /**
     * The fallback contents if the tab list is empty.
     */
    public static final TabListInformation EMPTY = new TabListInformation(List.of(), List.of(), List.of(), List.of(), Map.of(), 0, 0, 0, 0, 0, 0, true, null);

    /**
     * Stores the current state of a player's health, as shown in the tab list.
     */
    public static class HealthState {
        private static final long DISPLAY_UPDATE_DELAY = 20L;
        private static final long DECREASE_BLINK_DURATION = 20L;
        private static final long INCREASE_BLINK_DURATION = 10L;

        private int lastValue;
        private int displayedValue;
        private long lastUpdateTick;
        private long blinkUntilTick;

        public HealthState(int display) {
            this.displayedValue = display;
            this.lastValue = display;
        }

        /**
         * Updates the current value of these hearts.
         * Returns true if the hearts started or stopped blinking.
         */
        public boolean update(int value, long currentTick) {
            if (value != this.lastValue) {
                var blinkTime = value < this.lastValue ? DECREASE_BLINK_DURATION : INCREASE_BLINK_DURATION;
                this.blinkUntilTick = currentTick + blinkTime;
                this.lastValue = value;
                this.lastUpdateTick = currentTick;
            }

            if (currentTick - this.lastUpdateTick > DISPLAY_UPDATE_DELAY) {
                this.displayedValue = value;
            }
            return !isDoneBlinking(currentTick);
        }

        /**
         * Returns the currently displayed value.
         */
        public int displayedValue() {
            return this.displayedValue;
        }

        /**
         * Returns whether the hearts should blink in the current tick.
         */
        public boolean isBlinking(long currentTick) {
            return this.blinkUntilTick > currentTick && (this.blinkUntilTick - currentTick) % 6L >= 3L;
        }

        /**
         * Returns whether these hearts are done blinking or not.
         */
        public boolean isDoneBlinking(long currentTick) {
            return this.blinkUntilTick <= currentTick;
        }
    }
}
