package com.noxcrew.noxesium.feature.ui.layer;

import net.minecraft.client.gui.LayeredDraw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * The different contents of a NoxesiumLayeredDraw.
 */
public sealed interface NoxesiumLayer {

    /**
     * Stores a collection of nested layers.
     */
    final class NestedLayers implements NoxesiumLayer {

        private final NoxesiumLayeredDraw inner;
        private final List<NestedLayers> groups;
        private final BooleanSupplier condition;
        private boolean conditionResult = false;
        private boolean changedRecently = false;

        public NestedLayers(NoxesiumLayeredDraw inner, BooleanSupplier condition) {
            this.inner = inner;
            this.condition = condition;

            // Pre-filter which groups are a layer group object
            this.groups = new ArrayList<>();
            for (var layer : layers()) {
                if (layer instanceof NestedLayers group) {
                    this.groups.add(group);
                }
            }
        }

        /**
         * Returns the inner layered draw object.
         * We keep this object around as some other mods may add
         * layers after initialization.
         */
        public NoxesiumLayeredDraw inner() {
            return inner;
        }

        /**
         * Returns the layers in this group.
         */
        public List<NoxesiumLayer> layers() {
            return inner.layers();
        }

        /**
         * Returns whether this group's condition has recently changed.
         */
        public boolean hasChangedRecently() {
            return changedRecently;
        }

        /**
         * Returns the condition result.
         */
        public boolean test() {
            return conditionResult;
        }

        /**
         * Updates the current condition result.
         */
        public void update() {
            var oldResult = conditionResult;
            conditionResult = condition.getAsBoolean();
            changedRecently = oldResult != conditionResult;

            // Recursively call this for all layers in this group too!
            for (var group : groups) {
                group.update();
            }
        }
    }

    /**
     * Stores a singular layer that is always rendered.
     */
    record Layer(
            int index,
            String name,
            LayeredDraw.Layer layer
    ) implements NoxesiumLayer {

        // A global variable used for layer indices!
        private static int LAST_LAYER_INDEX = -1;
        private static final Map<Integer, String> STANDARD_LAYER_NAMES = new HashMap<>();

        static {
            STANDARD_LAYER_NAMES.put(0, "Camera Overlays");
            STANDARD_LAYER_NAMES.put(1, "Crosshair");
            STANDARD_LAYER_NAMES.put(2, "Hotbar");
            STANDARD_LAYER_NAMES.put(3, "XP Level");
            STANDARD_LAYER_NAMES.put(4, "Effects");
            STANDARD_LAYER_NAMES.put(5, "Bossbar");
            STANDARD_LAYER_NAMES.put(6, "Demo Overlay");
            STANDARD_LAYER_NAMES.put(7, "Debug Overlay");
            STANDARD_LAYER_NAMES.put(8, "Scoreboard");
            STANDARD_LAYER_NAMES.put(9, "Actionbar");
            STANDARD_LAYER_NAMES.put(10, "Title");
            STANDARD_LAYER_NAMES.put(11, "Chat");
            STANDARD_LAYER_NAMES.put(12, "Tab List");
            STANDARD_LAYER_NAMES.put(13, "Subtitles");

            // The sleep overlay ends up ordered as 7th but is defined last.
            STANDARD_LAYER_NAMES.put(14, "Sleep Overlay");
        }

        public Layer(LayeredDraw.Layer layer) {
            this(++LAST_LAYER_INDEX, STANDARD_LAYER_NAMES.getOrDefault(LAST_LAYER_INDEX, "Layer #" + LAST_LAYER_INDEX), layer);
        }

        public Layer(String name, LayeredDraw.Layer layer) {
            this(++LAST_LAYER_INDEX, name, layer);
        }
    }
}
