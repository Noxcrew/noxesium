package com.noxcrew.noxesium.feature.ui.render;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.ui.render.api.PerSecondRepeatingTask;
import com.noxcrew.noxesium.feature.ui.render.buffer.ElementBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages a buffer that gets re-used every time the UI framerate limit
 * is hit.
 */
public class StaticBuffer extends Element {

    private final List<ElementBuffer> buffers = new ArrayList<>();
    private final PerSecondRepeatingTask nextRender =
            new PerSecondRepeatingTask(NoxesiumMod.getInstance().getConfig().maxUiFramerate);

    @Override
    public List<ElementBuffer> getBuffers() {
        return buffers;
    }

    @Override
    public ElementBuffer createBuffer() {
        return new ElementBuffer();
    }

    @Override
    public boolean shouldRedraw(long nanoTime) {
        return nextRender.canInvoke(nanoTime);
    }
}
