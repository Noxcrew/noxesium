package com.noxcrew.noxesium.feature.render.cache.fps;

import com.noxcrew.noxesium.config.NoxesiumConfig;
import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.font.BakedComponentBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Adds a custom layer for Noxesium's fps overlay.
 */
public class FpsOverlayCache extends ElementCache<FpsOverlayInformation> {

    private static FpsOverlayCache instance;

    /**
     * Returns the current instance of this fps overlay cache.
     */
    public static FpsOverlayCache getInstance() {
        if (instance == null) {
            instance = new FpsOverlayCache();
        }
        return instance;
    }

    public FpsOverlayCache() {
        registerVariable("fps", (minecraft, partialTicks) -> Minecraft.getInstance().getFps());
    }

    @Override
    protected FpsOverlayInformation createCache(Minecraft minecraft, Font font) {
        var fpsComponent = new BakedComponentBuilder(Component.translatable("debug.fps_overlay", (int) getVariable("fps")), font);
        fpsComponent.shadow = false;
        var noxesiumComponent = (NoxesiumConfig.experimentalPatchesHotkey == null ? null : new BakedComponentBuilder(Component.translatable("debug.noxesium_overlay." + (NoxesiumConfig.experimentalPatchesHotkey ? "on" : "off")), font));
        if (noxesiumComponent != null) {
            noxesiumComponent.shadow = false;
        }
        return new FpsOverlayInformation(fpsComponent.build(), noxesiumComponent == null ? null : noxesiumComponent.build());
    }

    @Override
    protected void render(GuiGraphics graphics, FpsOverlayInformation cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font, float partialTicks, boolean dynamic) {
        // FIXME Can't just check for a different mod like this that's trash
        var lineOffset = font.lineHeight + 5;
        var offset = FabricLoader.getInstance().isModLoaded("toggle-sprint-display") ? lineOffset : 0;

        if (!dynamic) {
            graphics.fill(3, 3 + offset, 6 + cache.fps().width, 6 + font.lineHeight + offset, -1873784752);
        }
        if (cache.fps().shouldDraw(dynamic)) {
            cache.fps().draw(graphics, font, 5, 5 + offset, 0xE0E0E0);
        }

        if (cache.noxesium() != null) {
            if (!dynamic) {
                graphics.fill(3, 3 + offset + lineOffset, 6 + cache.noxesium().width, 6 + font.lineHeight + offset + lineOffset, -1873784752);
            }
            if (cache.noxesium().shouldDraw(dynamic)) {
                cache.noxesium().draw(graphics, font, 5, 5 + offset + lineOffset, 0xE0E0E0);
            }
        }
    }
}
