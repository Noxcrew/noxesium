package com.noxcrew.noxesium.feature.render.cache;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.io.Closeable;

import static net.minecraft.client.Minecraft.ON_OSX;

/**
 * The basis for an element that caches information about
 * a single element of the UI.
 */
public abstract class ElementCache<T extends ElementInformation> implements Closeable {

    protected T cache = null;
    private ElementBuffer buffer = null;
    private boolean needsRedraw = true;

    /**
     * Creates a new cached information object. This should contain all information
     * necessary to render the element. Other functions should call {@link this#clearCache()}
     * whenever this information may have changed. Changes are NOT passively detected!
     */
    protected abstract T createCache();

    /**
     * Creates a new buffer. Can be modified to disable blending of the drawn element.
     */
    protected ElementBuffer createBuffer() {
        return new ElementBuffer();
    }

    /**
     * Renders the direct part of the element to the UI directly.
     */
    public void renderDirect(GuiGraphics graphics, T cache, int screenWidth, int screenHeight, Minecraft minecraft) {
        // Draw the buffered contents of the element to the screen as a base!
        var screenBuffer = getBuffer();
        screenBuffer.draw();
    }

    /**
     * Renders the buffered part of the element to the given buffer.
     */
    protected abstract void renderBuffered(GuiGraphics graphics, T cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font);

    /**
     * Returns the current cached scoreboard contents.
     */
    public T getCache() {
        if (cache == null) {
            cache = createCache();
        }
        return cache;
    }

    /**
     * Returns the current buffer to use for drawing this element.
     * The buffer is automatically redrawn if it's not up-to-date.
     */
    public ElementBuffer getBuffer() {
        RenderSystem.assertOnRenderThread();
        var minecraft = Minecraft.getInstance();

        // Create the buffer and ensure it has the correct size
        if (buffer == null) {
            buffer = createBuffer();
        }
        if (buffer.resize(minecraft.getWindow())) {
            needsRedraw = true;
        }

        // Redraw into the buffer if we have to
        if (needsRedraw && buffer.isValid()) {
            var target = this.buffer.getTarget();
            try {
                var graphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
                target.setClearColor(0f, 0f, 0f, 0f);
                target.clear(ON_OSX);
                target.bindWrite(false);

                // Draw managed here to ensure we flush graphics buffers at the end!
                graphics.drawManaged(() -> {
                    renderBuffered(graphics, getCache(), minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), minecraft.font);
                });
            } finally {
                needsRedraw = false;
                target.unbindWrite();
                minecraft.getMainRenderTarget().bindWrite(true);
            }
        }
        return this.buffer;
    }

    /**
     * Clears the currently cached element contents. The frequency at which this is irrelevant as it's assumed
     * a server, even if it's constantly updating the UI, cannot do so more frequently than a fast enough
     * client would be attempting to render it. Any lowering of UI re-caching from client fps to server
     * update rate is a noticeable improvement.
     * <p>
     * Specifically for scoreboards:
     * While we tend to only clear the cache if relevant, we always clear if any changes are made
     * to the scores of players, even if said players' scores are not visible on the scoreboard. This could
     * be optimized further but is deemed unnecessary. If your scoreboard has a lot of hidden players it's
     * probably not one that heavily needs optimization.
     */
    public void clearCache() {
        cache = null;
        needsRedraw = true;
    }

    @Override
    public void close() {
        if (buffer != null) {
            buffer.close();
        }
    }
}
