package com.noxcrew.noxesium.feature.render.cache;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.io.Closeable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static net.minecraft.client.Minecraft.ON_OSX;

/**
 * The basis for an element that caches information about
 * a single element of the UI.
 */
public abstract class ElementCache<T extends ElementInformation> implements Closeable {

    private static final Set<ElementCache<?>> caches = new HashSet<>();
    protected T cache = null;
    private ElementBuffer buffer = null;
    private boolean needsRedraw = true;

    /**
     * Returns a collection of all created caches.
     */
    public static Collection<ElementCache<?>> getAllCaches() {
        return caches;
    }

    public ElementCache() {
        caches.add(this);
    }

    /**
     * Creates a new cached information object. This should contain all information
     * necessary to render the element. Other functions should call {@link this#clearCache()}
     * whenever this information may have changed. Changes are NOT passively detected!
     */
    protected abstract T createCache(Minecraft minecraft, Font font);

    /**
     * Creates a new buffer. Can be modified to disable blending of the drawn element.
     */
    protected ElementBuffer createBuffer() {
        return new ElementBuffer(getClass());
    }

    /**
     * Renders the UI element.
     */
    public void render(GuiGraphics graphics) {
        var minecraft = Minecraft.getInstance();
        var screenWidth = graphics.guiWidth();
        var screenHeight = graphics.guiHeight();
        render(graphics, screenWidth, screenHeight, minecraft);
    }

    /**
     * Renders the UI element.
     */
    public void render(GuiGraphics graphics, int screenWidth, int screenHeight, Minecraft minecraft) {
        try {
            // Draw the buffered contents of the element to the screen as a base!
            var screenBuffer = getBuffer();
            screenBuffer.draw();

            // Draw the direct parts on top each tick
            render(graphics, cache, minecraft, screenWidth, screenHeight, minecraft.font, false);
        } finally {
            // Ensure we always properly flush the graphics after drawing a component!
            graphics.flush();
        }
    }

    /**
     * Renders the UI element using the same logic whether we are in the buffer or not.
     */
    protected abstract void render(GuiGraphics graphics, T cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font, boolean buffered);

    /**
     * Returns the current cached scoreboard contents.
     */
    public T getCache() {
        if (cache == null) {
            cache = createCache(Minecraft.getInstance(), Minecraft.getInstance().font);
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
                render(graphics, cache, minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), minecraft.font, true);
                graphics.flush();
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