package com.noxcrew.noxesium.feature.ui.render.screen;

import com.noxcrew.noxesium.feature.ui.render.api.NoxesiumRenderState;
import com.noxcrew.noxesium.feature.ui.render.api.NoxesiumRenderStateHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

/**
 * Holds the screen rendering state.
 */
public class ScreenRenderingHolder implements NoxesiumRenderStateHolder<NoxesiumScreenRenderState> {

    private static final ScreenRenderingHolder instance = new ScreenRenderingHolder();
    private NoxesiumScreenRenderState state;

    /**
     * Returns the screen rendering holder instance.
     */
    public static ScreenRenderingHolder getInstance() {
        return instance;
    }

    /**
     * Renders the on-screen menu.
     */
    public boolean render(GuiGraphics guiGraphics, int width, int height, float deltaTime, Screen screen) {
        if (state == null) {
            state = new NoxesiumScreenRenderState();
        }
        return state.render(guiGraphics, width, height, deltaTime, screen);
    }

    @Override
    public @Nullable NoxesiumRenderState get() {
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
