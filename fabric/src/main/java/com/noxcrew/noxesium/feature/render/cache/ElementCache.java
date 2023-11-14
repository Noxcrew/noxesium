package com.noxcrew.noxesium.feature.render.cache;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.io.Closeable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import static net.minecraft.client.Minecraft.ON_OSX;

/**
 * The basis for an element that caches information about
 * a single element of the UI.
 */
public abstract class ElementCache<T extends ElementInformation> implements Closeable {

    private static final Set<ElementCache<?>> caches = new HashSet<>();

    private final Map<String, BiFunction<Minecraft, Float, Object>> variables = new HashMap<>();
    private final Map<String, Object> values = new HashMap<>();
    private ElementBuffer buffer = null;
    private boolean needsRedraw = true;
    protected T cache = null;


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
     * Registers a new variable that is re-evaluated each time the element is drawn which will
     * cause a cache clear if it changes.
     */
    public void registerVariable(String name, BiFunction<Minecraft, Float, Object> function) {
        Preconditions.checkState(!variables.containsKey(name), "Variable called " + name + " already exists");
        variables.put(name, function);
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
     * Returns whether cache is empty and nothing should be drawn.
     */
    protected abstract boolean isEmpty(T cache);

    /**
     * Renders the UI element.
     */
    public void render(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTicks, Minecraft minecraft) {
        var cache = getCache(minecraft, partialTicks);
        if (isEmpty(cache)) return;

        try {
            // Draw the buffered contents of the element to the screen as a base!
            var screenBuffer = getBuffer(cache);
            screenBuffer.draw();

            // Draw the direct parts on top each tick
            render(graphics, cache, minecraft, screenWidth, screenHeight, minecraft.font, partialTicks, false);
        } finally {
            // Ensure we always properly flush the graphics after drawing a component!
            graphics.flush();
        }
    }

    /**
     * Renders the UI element using the same logic whether we are in the buffer or not.
     */
    protected abstract void render(GuiGraphics graphics, T cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font, float partialTicks, boolean buffered);

    /**
     * Returns the value of the variable called name cast as T.
     */
    public <V> V getVariable(String name) {
        return (V) values.get(name);
    }

    /**
     * Returns the current cached scoreboard contents.
     */
    public T getCache(Minecraft minecraft, float partialTicks) {
        // Test all variables and clear the cache if any change
        if (!variables.isEmpty()) {
            for (var variable : variables.entrySet()) {
                var currentValue = values.get(variable.getKey());
                var newValue = variable.getValue().apply(minecraft, partialTicks);
                if (Objects.equals(currentValue, newValue) && currentValue != null) continue;

                // Clear the cache and ensure all variables are determined!
                clearCache();
                for (var otherVariable : variables.entrySet()) {
                    if (Objects.equals(variable.getKey(), otherVariable.getKey())) {
                        values.put(variable.getKey(), newValue);
                    } else {
                        values.put(otherVariable.getKey(), otherVariable.getValue().apply(minecraft, partialTicks));
                    }
                }
                break;
            }
        }

        if (cache == null) {
            cache = createCache(minecraft, minecraft.font);
        }
        return cache;
    }

    /**
     * Returns the current buffer to use for drawing this element.
     * The buffer is automatically redrawn if it's not up-to-date.
     */
    public ElementBuffer getBuffer(T cache) {
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
                render(graphics, cache, minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), minecraft.font, 0f, true);
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
        values.clear();
        needsRedraw = true;
    }

    @Override
    public void close() {
        if (buffer != null) {
            buffer.close();
        }
    }
}
