package com.noxcrew.noxesium.feature.ui.cache;

import com.google.common.base.Preconditions;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.NoxesiumMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static net.minecraft.client.Minecraft.ON_OSX;

/**
 * Wraps a single UI element. Handles caching previously drawn frames of the UI element
 * if no changes are detected in it.
 * <p>
 * If [requestRedraw] is called a new frame is drawn immediately on the next frame. Alternatively
 * if some variable changes a redraw also happens immediately on the next frame.
 * <p>
 * Beyond that UI elements are drawn at most 20 times per second, that is, a new UI frame is drawn
 * only after 50ms have passed since the start of the last draw call. This is more than reasonable
 * especially since we use variables for fades/animations to make them frame accurate (as we could
 * otherwise not pick a reasonable limit without judging the smoothness of some value). That is,
 * giving a configuration option to pick the frame rate to use for the UI causes people to configure
 * this value based on their judgement of smoothness of the UI. We don't want the desired smoothness
 * of rare events to make people run many unnecessary updates at all times. We should detect animations
 * and stop optimizing until they are over, re-enabling optimizations when they are useful.
 * <p>
 * The intent is for this 20 times per second cap to only apply to complicated or trivial effects.
 * E.g. the F3 screen or blinking hearts in TAB. Both effects may chance on any frame but they are not
 * animations and the user won't actually need to see what happens on the frames we skip.
 * <p>
 * For any animations such as text fading in we should ensure this gets animated correctly.
 * <p>
 * ----
 * <p>
 * We apply a complicated system to determine when redrawing needs to happen outside of specific requests.
 * This is necessary because there are a number of effects that could cause any piece of UI to become unpredictable:
 * - Another mod can inject some unexpected behavior
 * - Any obfuscated text changes character constantly
 * - Text shaders can rely on the game time to completely change text rendering
 * <p>
 * This is why we go for a dynamic approach, we always render 2 frames in a row and if the second frame is identical
 * to the first we skip a frame, if frame 4 still matches we skip 2 frames, etc. This allows for a slow ramp into
 * the possible maximum frame skippage without missing out on animations that we just didn't catch. In addition to
 * this we always render 1 frame every second anyway to ensure we don't get too out of sync, but if that frame
 * stays identical we wait for the next full second before trying again. (so after 1 second of no changes we basically
 * render the UI at 1 fps)
 * <p>
 * ----
 * <p>
 * This feature has gone through various iterations but has now landed on the approach of optimizing
 * around the UI code instead of directly reworking the UI code itself. It's up to Mojang to make optimizations
 * to individual UI components, or to improve text rendering in general, but this requires too many
 * changes to too much code which causes a lot of instability with other projects, and prevents Noxesium
 * from being able to support any of them. Instead, the approach is to optimize when we render frames
 * in a friendly way while still obtaining maximum performance.
 * <p>
 * The small loss of performance by not drawing text 2% more efficiently is greatly outweighed by even
 * a 2x reduction in frames on which UI is rendered. We can settle for drawing UI on 6 instead of 2 frames
 * within a 1 second window on 60fps. Actually drawing a UI is not terribly performance demanding anyway,
 * it's just the repetition.
 */
public abstract class ElementWrapper {

    public static boolean allowBlendChanges;

    private final Map<String, BiFunction<Minecraft, DeltaTracker, Object>> variables = new HashMap<>();
    private final Map<String, Object> values = new HashMap<>();
    private ElementBuffer buffer;
    private boolean needsRedraw = true;

    /**
     * Returns the value of the variable called name cast as T.
     */
    public final <V> V getVariable(String name) {
        // Fallback to ensure there is always some data!
        if (!values.containsKey(name)) {
            return (V) variables.get(name).apply(Minecraft.getInstance(), DeltaTracker.ZERO);
        }
        return (V) values.get(name);
    }

    /**
     * Registers a new variable that is re-evaluated each time the element is drawn which will
     * cause a cache clear if it changes.
     */
    public final void registerVariable(String name, BiFunction<Minecraft, DeltaTracker, Object> function) {
        Preconditions.checkState(!variables.containsKey(name), "Variable called " + name + " already exists");
        variables.put(name, function);
    }

    /**
     * Tests if any variables have changed, and if so, requests a redraw.
     */
    private void testVariableChanges(Minecraft minecraft, DeltaTracker deltaTracker) {
        // Test all variables and clear the cache if any change
        if (!variables.isEmpty()) {
            for (var variable : variables.entrySet()) {
                var currentValue = values.get(variable.getKey());
                var newValue = variable.getValue().apply(minecraft, deltaTracker);
                if (Objects.equals(currentValue, newValue) && currentValue != null) continue;

                // Clear the cache and ensure all variables are determined!
                requestRedraw();
                for (var otherVariable : variables.entrySet()) {
                    if (Objects.equals(variable.getKey(), otherVariable.getKey())) {
                        values.put(variable.getKey(), newValue);
                    } else {
                        values.put(otherVariable.getKey(), otherVariable.getValue().apply(minecraft, deltaTracker));
                    }
                }
                break;
            }
        }
    }

    /**
     * Requests that the element is redrawn the next frame, if it's not already being redrawn.
     */
    public final void requestRedraw() {
        values.clear();
        needsRedraw = true;
    }

    /**
     * Renders this UI element while wrapping existing code for doing so provided by operation of a method
     * of type void method(GuiGraphics, DeltaTracker);
     */
    public final void wrapOperation(GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> operation) {
        if (NoxesiumMod.getInstance().getConfig().shouldDisableExperimentalPerformancePatches()) {
            operation.call(graphics, deltaTracker);
        } else {
            render(graphics, deltaTracker, (grph) -> operation.call(grph, deltaTracker));
        }
    }

    /**
     * @see ElementWrapper#wrapOperation(GuiGraphics, DeltaTracker, Operation)
     */
    public final void wrapOperation(GuiGraphics graphics, Operation<Void> operation) {
        if (NoxesiumMod.getInstance().getConfig().shouldDisableExperimentalPerformancePatches()) {
            operation.call(graphics);
        } else {
            render(graphics, DeltaTracker.ZERO, operation::call);
        }
    }

    /**
     * @see ElementWrapper#wrapOperation(GuiGraphics, DeltaTracker, Operation)
     */
    public final void wrapOperation(GuiGraphics graphics, int partialTicks, Scoreboard scoreboard, Objective objective, Operation<Void> operation) {
        if (NoxesiumMod.getInstance().getConfig().shouldDisableExperimentalPerformancePatches()) {
            operation.call(graphics, partialTicks, scoreboard, objective);
        } else {
            render(graphics, DeltaTracker.ZERO, (grph) -> operation.call(grph, partialTicks, scoreboard, objective));
        }
    }

    /**
     * Renders the UI element.
     */
    public final void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        render(graphics, deltaTracker, (grph) -> {});
    }

    /**
     * Renders the UI element.
     */
    public final void render(GuiGraphics graphics, DeltaTracker deltaTracker, Consumer<GuiGraphics> renderFunction) {
        // Test if any variables have changed
        var minecraft = Minecraft.getInstance();
        testVariableChanges(minecraft, deltaTracker);

        try {
            // Flush any remaining buffer from a previous element
            graphics.flush();

            // Draw the buffered contents of the element to the screen as a base!
            RenderSystem.assertOnRenderThread();

            // Create the buffer and ensure it has the correct size
            if (buffer == null) {
                buffer = new ElementBuffer();
            }

            // Try to re-size the buffer
            if (buffer.resize(minecraft.getWindow())) {
                needsRedraw = true;
            }

            // Redraw into the buffers if we have to
            if (needsRedraw && buffer.isValid()) {
                var target = buffer.getTarget();
                try {
                    target.setClearColor(0, 0, 0, 0);
                    target.clear(ON_OSX);
                    target.bindWrite(false);
                    renderFunction.accept(graphics);
                    render(graphics, minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), minecraft.font, deltaTracker);
                } finally {
                    graphics.flush();
                    needsRedraw = false;
                    target.unbindWrite();
                    minecraft.getMainRenderTarget().bindWrite(true);
                }
            }

            if (buffer.isValid()) {
                buffer.draw();
            }
        } finally {
            // Ensure we always properly flush the graphics after drawing a component!
            graphics.flush();
        }
    }

    /**
     * Performs additional rendering logic for this element. Not applicable to cases where an operation is being wrapped.
     */
    protected void render(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int screenHeight, Font font, DeltaTracker deltaTracker) {
    }

    /**
     * Runs the given runnable and sets back the blending state after.
     */
    public static void withBlend(Runnable configure, Runnable runnable) {
        // Cache the current blend state so we can return to it
        final var currentBlend = GlStateManager.BLEND.mode.enabled;
        final var srcRgb = GlStateManager.BLEND.srcRgb;
        final var dstRgb = GlStateManager.BLEND.dstRgb;
        final var srcAlpha = GlStateManager.BLEND.srcAlpha;
        final var dstAlpha = GlStateManager.BLEND.dstAlpha;

        configure.run();
        allowBlendChanges = false;
        runnable.run();
        allowBlendChanges = true;

        // Restore the original state
        if (currentBlend) {
            RenderSystem.enableBlend();
        } else {
            RenderSystem.disableBlend();
        }
        GlStateManager._blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
    }
}
