package com.noxcrew.noxesium.feature.ui.render;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a buffer that gets re-used every time the UI framerate limit
 * is hit.
 */
public class StaticBuffer extends Element {

    private final List<ElementBuffer> buffers = new ArrayList<>();

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
        return true;
    }
}
