package com.noxcrew.noxesium.feature.render.cache.bossbar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import com.noxcrew.noxesium.feature.render.font.GuiGraphicsExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;

import java.util.ArrayList;

/**
 * Manages the current cache of the boss bar.
 */
public class BossBarCache extends ElementCache<BossBarInformation> {

    private static BossBarCache instance;
    private static final int HEIGHT = 12;
    private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("textures/gui/bars.png");

    /**
     * Returns the current instance of this boss bar cache.
     */
    public static BossBarCache getInstance() {
        if (instance == null) {
            instance = new BossBarCache();
        }
        return instance;
    }

    /**
     * Creates newly cached boss bar content information.
     * <p>
     * Depends on the following information:
     * - Current resource pack configuration
     * - Current boss bar contents
     * - Whether the boss bar is interpolating
     */
    @Override
    public BossBarInformation createCache() {
        System.out.println("Buliding boss bar cache");
        var overlay = Minecraft.getInstance().gui.getBossOverlay();
        if (overlay.events.isEmpty()) {
            return BossBarInformation.EMPTY;
        }

        var font = Minecraft.getInstance().font;
        var bars = new ArrayList<BossBar>();
        for (var entry : overlay.events.entrySet()) {
            var bar = entry.getValue();
            var animating = Math.abs(bar.getProgress() - bar.targetPercent) >= 0.001;
            bars.add(new BossBar(
                    new BakedComponent(bar.getName()),
                    font.width(bar.getName()),
                    bar,
                    bar.getOverlay(),
                    bar.getColor(),
                    animating
            ));
        }
        return new BossBarInformation(bars);
    }

    @Override
    public void renderDirect(GuiGraphics graphics, BossBarInformation cache, int screenWidth, int screenHeight, Minecraft minecraft) {
        super.renderDirect(graphics, cache, screenWidth, screenHeight, minecraft);

        var clearCache = false;
        if (!cache.bars().isEmpty()) {
            var currentHeight = HEIGHT;
            for (var bossbar : cache.bars()) {
                if (bossbar.animating()) {
                    // Draw the main bars
                    var barLeft = screenWidth / 2 - 91;
                    this.drawBar(graphics, barLeft, currentHeight, bossbar, 182, 0);
                    var progress = (int) (bossbar.bar().getProgress() * 183.0F);
                    if (progress > 0) {
                        this.drawBar(graphics, barLeft, currentHeight, bossbar, progress, 5);
                    }

                    // If any bar has finished animating we clear the cache
                    if (Math.abs(bossbar.bar().getProgress() - bossbar.bar().targetPercent) < 0.001) {
                        clearCache = true;
                    }
                }

                // Draw the text above
                if (bossbar.name().hasObfuscation) {
                    var left = screenWidth / 2 - bossbar.barWidth() / 2;
                    var bottom = currentHeight - 9;
                    GuiGraphicsExt.drawString(graphics, minecraft.font, bossbar.name(), left, bottom, 16777215, true);
                }

                currentHeight += 10 + 9;
                if (currentHeight >= screenHeight / 3) {
                    break;
                }
            }
        }

        if (clearCache) {
            clearCache();
        }
    }

    @Override
    public void renderBuffered(GuiGraphics graphics, BossBarInformation cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font) {
        if (!cache.bars().isEmpty()) {
            var currentHeight = HEIGHT;
            for (var bossbar : cache.bars()) {
                if (!bossbar.animating()) {
                    // Draw the main bars
                    var barLeft = screenWidth / 2 - 91;
                    this.drawBar(graphics, barLeft, currentHeight, bossbar, 182, 0);
                    var progress = (int) (bossbar.bar().getProgress() * 183.0F);
                    if (progress > 0) {
                        this.drawBar(graphics, barLeft, currentHeight, bossbar, progress, 5);
                    }
                }

                // Draw the text above
                if (!bossbar.name().hasObfuscation) {
                    var left = screenWidth / 2 - bossbar.barWidth() / 2;
                    var bottom = currentHeight - 9;
                    GuiGraphicsExt.drawString(graphics, minecraft.font, bossbar.name(), left, bottom, 16777215, true);
                }

                currentHeight += 10 + 9;
                if (currentHeight >= screenHeight / 3) {
                    break;
                }
            }
        }
    }

    /**
     * Draws a single boss bar background.
     */
    private void drawBar(GuiGraphics guiGraphics, int x, int y, BossBar bossBar, int uvWidth, int uvOffset) {
        guiGraphics.blit(GUI_BARS_LOCATION, x, y, 0, bossBar.color().ordinal() * 5 * 2 + uvOffset, uvWidth, 5);
        if (bossBar.overlay() != BossEvent.BossBarOverlay.PROGRESS) {
            RenderSystem.enableBlend();
            guiGraphics.blit(GUI_BARS_LOCATION, x, y, 0, 80 + (bossBar.overlay().ordinal() - 1) * 5 * 2 + uvOffset, uvWidth, 5);
            RenderSystem.disableBlend();
        }
    }
}
