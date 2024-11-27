package com.noxcrew.noxesium.feature.ui.layer;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.ui.LayerWithReference;
import com.noxcrew.noxesium.feature.ui.render.NoxesiumUiRenderState;
import com.noxcrew.noxesium.feature.ui.render.api.NoxesiumRenderState;
import com.noxcrew.noxesium.feature.ui.render.api.NoxesiumRenderStateHolder;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A custom implementation of layered draw that persists groupings
 * of layers to properly be able to distinguish between them.
 */
public class NoxesiumLayeredDraw implements LayeredDraw.Layer, NoxesiumRenderStateHolder<NoxesiumUiRenderState> {

    private final List<NoxesiumLayer> layers = new ArrayList<>();
    private final List<NoxesiumLayer.NestedLayers> subgroups = new ArrayList<>();
    private final List<NoxesiumLayeredDraw> parents = new ArrayList<>();
    private int size = -1;
    private NoxesiumUiRenderState state;

    /**
     * Returns an unmodifiable copy of this object's layers.
     */
    public List<NoxesiumLayer> layers() {
        return Collections.unmodifiableList(layers);
    }

    /**
     * Returns all groups within this layered draw.
     */
    public List<NoxesiumLayer.NestedLayers> subgroups() {
        return subgroups;
    }

    /**
     * Adds a new layer to this object.
     */
    public void add(NoxesiumLayer layer) {
        layers.add(layer);
        if (layer instanceof NoxesiumLayer.NestedLayers nested) {
            subgroups.add(nested);

            // Add this to the parents of the other so we can update it.
            nested.inner().parents.add(this);
        }

        // Trigger a recursive update to the parents
        update();
    }

    @Override
    public void render(GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        // If experimental patches are disabled we ignore all custom logic.
        if (NoxesiumMod.getInstance().getConfig().shouldDisableExperimentalPerformancePatches()) {
            // Destroy the state if it exists
            if (state != null) {
                state.close();
                state = null;
            }

            // Directly draw everything to the screen
            guiGraphics.pose().pushPose();
            for (var layer : layers) {
                renderLayerDirectly(guiGraphics, deltaTracker, layer);
            }
            guiGraphics.pose().popPose();
            return;
        }

        // Defer rendering the object to the current state, this holds all necessary information
        // for rendering.
        if (state == null) {
            // Create a new state object if we don't have one yet, we do this here because
            // not all LayeredDraw objects are actually used for rendering because Mojang
            // uses them for sub-groups as well.
            state = new NoxesiumUiRenderState();
        }
        state.render(guiGraphics, deltaTracker, this);
    }

    /**
     * Renders a single layer directly, avoiding all custom UI optimizations.
     */
    private void renderLayerDirectly(GuiGraphics guiGraphics, DeltaTracker deltaTracker, NoxesiumLayer layer) {
        switch (layer) {
            case NoxesiumLayer.Layer single -> {
                single.layer().render(guiGraphics, deltaTracker);
                guiGraphics.pose().translate(0f, 0f, LayeredDraw.Z_SEPARATION);
            }
            case NoxesiumLayer.NestedLayers group -> {
                if (group.condition().getAsBoolean()) {
                    for (var subLayer : group.layers()) {
                        renderLayerDirectly(guiGraphics, deltaTracker, subLayer);
                    }
                }
            }
        }
    }

    /**
     * Returns a flattened list of this object.
     */
    public List<LayerWithReference> flatten() {
        var result = new ArrayList<LayerWithReference>();
        for (var layer : layers) {
            switch (layer) {
                case NoxesiumLayer.Layer single -> result.add(new LayerWithReference(result.size(), single, null));
                case NoxesiumLayer.NestedLayers group -> process(group, result);
            }
        }
        return result;
    }

    /**
     * Adds the contents of the layer group to the given list.
     */
    private void process(NoxesiumLayer.NestedLayers target, List<LayerWithReference> list) {
        for (var layer : target.layers()) {
            switch (layer) {
                case NoxesiumLayer.Layer single -> list.add(new LayerWithReference(list.size(), single, target));
                case NoxesiumLayer.NestedLayers group -> process(group, list);
            }
        }
    }

    /**
     * Returns the size of this layered draw.
     */
    public int size() {
        // When the size is -1 we need to re-determine the value
        if (size == -1) {
            for (var layer : layers) {
                switch (layer) {
                    case NoxesiumLayer.Layer ignored -> size++;
                    case NoxesiumLayer.NestedLayers group -> size += group.inner().size();
                }
            }
        }
        return size;
    }

    /**
     * Updates the size of this object and its parents.
     */
    private void update() {
        size = -1;
        parents.forEach(NoxesiumLayeredDraw::update);
    }

    @Nullable
    @Override
    public NoxesiumRenderState get() {
        return state;
    }

    @Override
    public void clear() {
        if (state != null) {
            state.close();
            state = null;
        }
    }
}
