package com.noxcrew.noxesium.feature.ui.render;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.ui.BufferHelper;
import com.noxcrew.noxesium.feature.ui.layer.NoxesiumLayeredDraw;
import com.noxcrew.noxesium.feature.ui.render.api.NoxesiumRenderState;
import com.noxcrew.noxesium.feature.ui.render.buffer.BufferData;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Stores the entire render state of the current UI.
 */
public class NoxesiumUiRenderState extends NoxesiumRenderState {

    private final List<LayerGroup> groups = new CopyOnWriteArrayList<>();
    private final Random random = new Random();
    private long nextUpdate = -1;
    private int lastSize = 0;

    /**
     * Returns all groups in this render state.
     */
    public List<LayerGroup> groups() {
        return groups;
    }

    /**
     * Renders the given layered draw object to the screen.
     */
    public boolean render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, NoxesiumLayeredDraw layeredDraw) {
        var nanoTime = System.nanoTime();
        var dynamic = NoxesiumMod.getInstance().getConfig().shouldUseDynamicUiLimiting();

        // Update all groups, re-ordering where each layer is located
        updateGroups(layeredDraw, nanoTime, dynamic);

        // Tick the groups, possibly redrawing the buffer contents, if any buffers got drawn to
        // we want to unbind the buffer afterwards
        for (var group : groups) {
            // Determine if the group has recently changed their
            // visibility state, if so request an immediate redraw!
            for (var layer : group.layers()) {
                if (layer.groups() == null) continue;
                for (var layerGroup : layer.groups()) {
                    if (layerGroup.hasChangedRecently()) {
                        group.dynamic().redraw();
                        break;
                    }
                }
            }

            // Update the dynamic element of the group
            group.dynamic().update(nanoTime, guiGraphics, () -> {
                outer:
                for (var layer : group.layers()) {
                    if (layer.groups() != null) {
                        for (var layerGroup : layer.groups()) {
                            if (!layerGroup.test()) {
                                continue outer;
                            }
                        }
                    }
                    group.renderLayer(guiGraphics, deltaTracker, layer.layer(), layer.index());
                }
            });
        }

        // Unbind the frame buffers
        BufferHelper.unbind();

        // If any group is invalid we give up
        for (var group : groups) {
            if (group.dynamic().isInvalid()) return false;
        }

        // Draw all groups to the screen together
        var ids = new ArrayList<BufferData>();
        for (var group : groups) {
            group.dynamic().submitTextureIds(ids);
        }
        SharedVertexBuffer.draw(ids);
        renders.increment();
        return true;
    }

    /**
     * Updates which groups are currently registered in this render state.
     * Also ticks their visibility state.
     */
    private void updateGroups(NoxesiumLayeredDraw layeredDraw, long nanoTime, boolean dynamic) {
        // Update which groups exist if the layered draw object has changed
        var intendedSize = dynamic ? layeredDraw.size() : 1;
        if (lastSize != intendedSize) {
            lastSize = intendedSize;
            resetGroups();

            // Determine all layers ordered and flattened, then
            // split them up into partitions if we are in dynamic mode
            var flattened = layeredDraw.flatten();
            if (dynamic) {
                var chunked = chunked(flattened, flattened.size() / 4);
                for (var chunk : chunked) {
                    var group = new LayerGroup();
                    group.addLayers(chunk);
                    groups.add(group);
                }
            } else {
                var group = new LayerGroup();
                group.addLayers(flattened);
                groups.add(group);
            }
        }

        // Update for each group what the condition is
        for (var group : layeredDraw.subgroups()) {
            group.update();
        }

        // If we're in dynamic mode we try to update groups.
        if (dynamic) {
            // Try to split up or merge together groups, but don't run this too frequently!
            if (nextUpdate == -1 || nanoTime >= nextUpdate) {
                // Schedule when we can next update the groups
                nextUpdate = nanoTime + random.nextLong(500000000);

                // Iterate through all groups and make changes
                var index = 0;
                while (index < groups.size()) {
                    var group = groups.get(index++);

                    // Try to merge if there are neighboring groups
                    if (index > 2 && index < groups.size()) {
                        if (group.canMerge(groups.get(index))) {
                            group.join(groups.get(index));
                            groups.remove(index).close();
                            index--;
                        } else if (groups.get(index - 2).canMerge(group)) {
                            groups.get(index - 2).join(group);
                            groups.remove(index - 1).close();
                            index--;
                        }
                    }

                    // Try to split up the group
                    if (group.shouldSplit()) {
                        groups.add(index++, group.split());
                    }
                }
            }
        }
    }

    /**
     * Destroys all previous groups.
     */
    private void resetGroups() {
        for (var group : groups) {
            group.close();
        }
        groups.clear();
    }

    @Override
    public void requestCheck() {
        for (var group : groups) {
            group.dynamic().requestCheck();
        }
    }

    @Override
    public void updateRenderFramerate() {
        for (var group : groups) {
            group.dynamic().resetToMax();
        }
    }

    @Override
    public void tick() {
        for (var group : groups) {
            group.dynamic().tick();
        }
    }

    @Override
    public void close() {
        resetGroups();
    }

    /**
     * Returns a chunked version of the input list where each chunk is at most [amount] large.
     */
    private <T> List<List<T>> chunked(List<T> input, int amount) {
        var result = new ArrayList<List<T>>();
        var index = 0;
        var total = input.size();
        while (index < total) {
            var group = Math.min(amount, total - index);
            result.add(input.subList(index, index + group));
            index += group;
        }
        return result;
    }
}
