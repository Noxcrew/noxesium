package com.noxcrew.noxesium.feature.render.cache.bossbar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.feature.render.cache.ElementBuffer;
import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import com.noxcrew.noxesium.feature.render.font.GuiGraphicsExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;

import java.util.ArrayList;

/**
 * Manages the current cache of the boss bar.
 */
public class BossBarCache extends ElementCache<BossBarInformation> {

    private static BossBarCache instance;
    private static final int HEIGHT = 12;
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;

    private static final ResourceLocation[] BAR_BACKGROUND_SPRITES = new ResourceLocation[]{new ResourceLocation("boss_bar/pink_background"), new ResourceLocation("boss_bar/blue_background"), new ResourceLocation("boss_bar/red_background"), new ResourceLocation("boss_bar/green_background"), new ResourceLocation("boss_bar/yellow_background"), new ResourceLocation("boss_bar/purple_background"), new ResourceLocation("boss_bar/white_background")};
    private static final ResourceLocation[] BAR_PROGRESS_SPRITES = new ResourceLocation[]{new ResourceLocation("boss_bar/pink_progress"), new ResourceLocation("boss_bar/blue_progress"), new ResourceLocation("boss_bar/red_progress"), new ResourceLocation("boss_bar/green_progress"), new ResourceLocation("boss_bar/yellow_progress"), new ResourceLocation("boss_bar/purple_progress"), new ResourceLocation("boss_bar/white_progress")};
    private static final ResourceLocation[] OVERLAY_BACKGROUND_SPRITES = new ResourceLocation[]{new ResourceLocation("boss_bar/notched_6_background"), new ResourceLocation("boss_bar/notched_10_background"), new ResourceLocation("boss_bar/notched_12_background"), new ResourceLocation("boss_bar/notched_20_background")};
    private static final ResourceLocation[] OVERLAY_PROGRESS_SPRITES = new ResourceLocation[]{new ResourceLocation("boss_bar/notched_6_progress"), new ResourceLocation("boss_bar/notched_10_progress"), new ResourceLocation("boss_bar/notched_12_progress"), new ResourceLocation("boss_bar/notched_20_progress")};

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
    protected BossBarInformation createCache() {
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
                    new BakedComponent(bar.getName(), font),
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
        if (cache.bars().isEmpty()) return;

        // Only draw the buffer if there are bars to draw!
        super.renderDirect(graphics, cache, screenWidth, screenHeight, minecraft);

        var clearCache = false;
        var currentHeight = HEIGHT;
        for (var bossbar : cache.bars()) {
            if (bossbar.animating() || bossbar.overlay() != BossEvent.BossBarOverlay.PROGRESS) {
                // Draw the main bars
                var barLeft = screenWidth / 2 - 91;
                this.drawBar(graphics, barLeft, currentHeight, bossbar, 182, BAR_BACKGROUND_SPRITES, OVERLAY_BACKGROUND_SPRITES, true, bossbar.overlay() != BossEvent.BossBarOverlay.PROGRESS);
                var progress = (int) (bossbar.bar().getProgress() * 183.0F);
                if (progress > 0) {
                    this.drawBar(graphics, barLeft, currentHeight, bossbar, progress, BAR_PROGRESS_SPRITES, OVERLAY_PROGRESS_SPRITES, true, bossbar.overlay() != BossEvent.BossBarOverlay.PROGRESS);
                }

                // If any bar has finished animating we clear the cache
                if (bossbar.animating() && Math.abs(bossbar.bar().getProgress() - bossbar.bar().targetPercent) < 0.001) {
                    clearCache = true;
                }
            }

            // Draw the text above
            if (bossbar.name().hasObfuscation) {
                var x = screenWidth / 2 - bossbar.name().width / 2;
                var y = currentHeight - 9;
                GuiGraphicsExt.drawString(graphics, minecraft.font, bossbar.name(), x, y, 16777215);
            }

            currentHeight += 10 + 9;
            if (currentHeight >= screenHeight / 3) {
                break;
            }
        }

        if (clearCache) {
            clearCache();
        }
    }

    @Override
    protected void renderBuffered(GuiGraphics graphics, BossBarInformation cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font) {
        if (cache.bars().isEmpty()) return;

        var currentHeight = HEIGHT;
        for (var bossbar : cache.bars()) {
            if (!bossbar.animating()) {
                // Draw the main bars
                var barLeft = screenWidth / 2 - 91;
                this.drawBar(graphics, barLeft, currentHeight, bossbar, 182, BAR_BACKGROUND_SPRITES, OVERLAY_BACKGROUND_SPRITES, bossbar.overlay() != BossEvent.BossBarOverlay.PROGRESS, false);
                var progress = (int) (bossbar.bar().getProgress() * 183.0F);
                if (progress > 0) {
                    this.drawBar(graphics, barLeft, currentHeight, bossbar, progress, BAR_PROGRESS_SPRITES, OVERLAY_PROGRESS_SPRITES, bossbar.overlay() != BossEvent.BossBarOverlay.PROGRESS, false);
                }
            }

            // Draw the text above
            if (!bossbar.name().hasObfuscation) {
                var x = screenWidth / 2 - bossbar.name().width / 2;
                var y = currentHeight - 9;
                GuiGraphicsExt.drawString(graphics, minecraft.font, bossbar.name(), x, y, 16777215);
            }

            currentHeight += 10 + 9;
            if (currentHeight >= screenHeight / 3) {
                break;
            }
        }
    }

    /**
     * Draws a single boss bar background.
     */
    private void drawBar(GuiGraphics guiGraphics, int x, int y, BossBar bossBar, int targetWidth, ResourceLocation[] bars, ResourceLocation[] overlays, boolean includeBase, boolean includeOverlay) {
        if (includeBase) {
            guiGraphics.blitSprite(bars[bossBar.color().ordinal()], BAR_WIDTH, BAR_HEIGHT, 0, 0, x, y, targetWidth, BAR_HEIGHT);
        }
        if (includeOverlay) {
            RenderSystem.enableBlend();
            guiGraphics.blitSprite(overlays[bossBar.overlay().ordinal() - 1], BAR_WIDTH, BAR_HEIGHT, 0, 0, x, y, targetWidth, BAR_HEIGHT);
            RenderSystem.disableBlend();
        }
    }
}
