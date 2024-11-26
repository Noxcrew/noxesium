package com.noxcrew.noxesium.feature.ui.render.api;

import com.mojang.blaze3d.platform.GlStateManager;
import com.noxcrew.noxesium.mixin.ui.ext.BooleanStateExt;
import com.noxcrew.noxesium.mixin.ui.ext.GlStateManagerExt;
import org.lwjgl.opengl.GL11;

/**
 * Holds a snapshot of the current blending state.
 */
public class BlendState {

    /**
     * Returns the disabled blending state.
     */
    public static BlendState off() {
        var state = new BlendState();
        state.blend = false;
        state.srcRgb = GL11.GL_SRC_ALPHA;
        state.dstRgb = GL11.GL_ONE_MINUS_SRC_ALPHA;
        state.srcAlpha = GL11.GL_ONE;
        state.dstAlpha = GL11.GL_ZERO;
        return state;
    }

    /**
     * Returns the standard blend state when rendering UI elements.
     */
    public static BlendState standard() {
        var state = new BlendState();
        state.blend = true;
        state.srcRgb = GL11.GL_SRC_ALPHA;
        state.dstRgb = GL11.GL_ONE_MINUS_SRC_ALPHA;
        state.srcAlpha = GL11.GL_ONE;
        state.dstAlpha = GL11.GL_ONE_MINUS_SRC_ALPHA;
        return state;
    }

    /**
     * Returns the glint blend state.
     */
    public static BlendState glint() {
        var state = new BlendState();
        state.blend = true;
        state.srcRgb = GL11.GL_SRC_COLOR;
        state.dstRgb = GL11.GL_ONE;
        state.srcAlpha = GL11.GL_ZERO;
        state.dstAlpha = GL11.GL_ONE;
        return state;
    }

    /**
     * Takes a snapshot of the current blend state
     * intended by vanilla.
     */
    public static BlendState snapshot() {
        var state = new BlendState();
        var blend = GlStateManagerExt.getBlendingState();
        state.blend = ((BooleanStateExt) blend.mode).isEnabled();
        state.srcRgb = blend.srcRgb;
        state.dstRgb = blend.dstRgb;
        state.srcAlpha = blend.srcAlpha;
        state.dstAlpha = blend.dstAlpha;
        return state;
    }

    /**
     * Creates a new blend state from the given input.
     */
    public static BlendState from(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
        var state = new BlendState();
        state.blend = true;
        state.srcRgb = srcRgb;
        state.dstRgb = dstRgb;
        state.srcAlpha = srcAlpha;
        state.dstAlpha = dstAlpha;
        return state;
    }

    private boolean blend;
    private int srcRgb, dstRgb, srcAlpha, dstAlpha;

    /**
     * Applies this blend state.
     */
    public void apply() {
        if (blend) {
            GlStateManager._enableBlend();
        } else {
            GlStateManager._disableBlend();
        }

        GlStateManager._blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
    }

    /**
     * Returns if the given values match this blend state.
     */
    public boolean matches(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
        return srcRgb == this.srcRgb && dstRgb == this.dstRgb && srcAlpha == this.srcAlpha && dstAlpha == this.dstAlpha;
    }

    @Override
    public String toString() {
        return "BlendState[" +
                "blend=" + blend +
                ", srcRgb=" + srcRgb +
                ", dstRgb=" + dstRgb +
                ", srcAlpha=" + srcAlpha +
                ", dstAlpha=" + dstAlpha +
                ']';
    }
}
