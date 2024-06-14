package com.noxcrew.noxesium.feature.render.cache.gametime;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.font.BakedComponentBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Adds a custom layer for Noxesium's game time overlay.
 */
public class GameTimeOverlayCache extends ElementCache<GameTimeOverlayInformation> {

    private static GameTimeOverlayCache instance;

    /**
     * Returns the current instance of this game time overlay cache.
     */
    public static GameTimeOverlayCache getInstance() {
        if (instance == null) {
            instance = new GameTimeOverlayCache();
        }
        return instance;
    }

    /**
     * Renders the game time overlay through its cache.
     */
    public static void renderGameTimeOverlay(GuiGraphics graphics, DeltaTracker deltaTracker) {
        getInstance().render(graphics, deltaTracker);
    }

    public GameTimeOverlayCache() {
        registerVariable("game time", (minecraft, partialTicks) -> RenderSystem.getShaderGameTime());
    }

    @Override
    protected GameTimeOverlayInformation createCache(Minecraft minecraft, Font font) {
        var gameTimeComponent = new BakedComponentBuilder(Component.translatable("debug.game_time_overlay", String.format("%.5f", (float) getVariable("game time")), (int) ((float) getVariable("game time") * 24000)), font);
        gameTimeComponent.shadow = false;
        return new GameTimeOverlayInformation(gameTimeComponent.build());
    }

    @Override
    protected void render(GuiGraphics graphics, GameTimeOverlayInformation cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font, DeltaTracker deltaTracker, boolean dynamic) {
        // FIXME Can't just check for a different mod like this that's trash
        var lineOffset = font.lineHeight + 5;
        var offset = FabricLoader.getInstance().isModLoaded("toggle-sprint-display") ? lineOffset * 2 : lineOffset;

        if (!dynamic) {
            graphics.fill(3, 3 + offset, 6 + cache.gameTime().width, 6 + font.lineHeight + offset, -1873784752);
        }
        if (cache.gameTime().shouldDraw(dynamic)) {
            cache.gameTime().draw(graphics, font, 5, 5 + offset, 0xE0E0E0);
        }
    }
}
