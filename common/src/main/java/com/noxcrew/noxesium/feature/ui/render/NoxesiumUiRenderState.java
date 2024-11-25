package com.noxcrew.noxesium.feature.ui.render;

import com.noxcrew.noxesium.feature.ui.BufferHelper;
import com.noxcrew.noxesium.feature.ui.layer.NoxesiumLayeredDraw;
import com.noxcrew.noxesium.feature.ui.render.api.BufferData;
import com.noxcrew.noxesium.feature.ui.render.api.NoxesiumRenderState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Stores the entire render state of the current UI.
 */
public class NoxesiumUiRenderState implements NoxesiumRenderState {

    private final List<ElementBufferGroup> groups = new CopyOnWriteArrayList<>();
    private final Random random = new Random();
    private final double updateFps = 0.5;
    private long nextUpdate = -1;
    private int lastSize = 0;

    /**
     * Returns all groups in this render state.
     */
    public List<ElementBufferGroup> groups() {
        return groups;
    }

    /**
     * Renders the given layered draw object to the screen.
     */
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, NoxesiumLayeredDraw layeredDraw) {
        var nanoTime = System.nanoTime();

        // Update which groups exist
        if (lastSize != layeredDraw.size()) {
            lastSize = layeredDraw.size();
            resetGroups();

            // Determine all layers ordered and flattened, then
            // split them up into
            var flattened = layeredDraw.flatten();

            // Start by splitting into 4 partitions
            var chunked = chunked(flattened, flattened.size() / 4);
            for (var chunk : chunked) {
                var group = new ElementBufferGroup();
                group.addLayers(chunk);
                groups.add(group);
            }
        }

        // Update for each group what the condition is
        for (var group : layeredDraw.subgroups()) {
            group.update();
        }

        // Try to split up or merge together groups, but don't run this too frequently!
        if (nextUpdate == -1) {
            nextUpdate = nanoTime;
        }
        if (nanoTime >= nextUpdate) {
            // Schedule when we can next update the groups
            nextUpdate = nanoTime + (long) Math.floor(((1 / updateFps) * random.nextDouble() * 1000000000));

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

        // Tick the groups, possibly redrawing the buffer contents, if any buffers got drawn to
        // we want to unbind the buffer afterwards
        for (var group : groups) {
            // Determine if the group has recently changed their
            // visibility state, if so request an immediate redraw!
            for (var layer : group.layers()) {
                if (layer.group() != null && layer.group().hasChangedRecently()) {
                    group.dynamic().redraw();
                    break;
                }
            }

            // Update the dynamic element of the group
            group.dynamic().update(nanoTime, guiGraphics, () -> {
                for (var layer : group.layers()) {
                    if (layer.group() == null || layer.group().test()) {
                        group.renderLayer(guiGraphics, deltaTracker, layer.layer(), layer.index());
                    }
                }
            });
        }

        // Unbind the frame buffers
        BufferHelper.unbind();

        // Draw the groups in order
        var ids = new ArrayList<BufferData>();
        for (var group : groups) {
            // If the buffer is broken we have to early exit and draw
            // directly before going back to the buffers!
            if (group.dynamic().isInvalid()) {
                SharedVertexBuffer.draw(ids);
                ids.clear();
                group.drawDirectly(guiGraphics, deltaTracker);
                continue;
            }

            // If the buffer is valid we use it to draw
            group.dynamic().submitTextureIds(ids);
        }

        // Call draw on any remaining ids
        SharedVertexBuffer.draw(ids);
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
            group.requestCheck();
        }
    }

    @Override
    public void updateRenderFramerate() {
        for (var group : groups) {
            group.updateRenderFramerate();
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
