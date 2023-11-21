package com.noxcrew.noxesium.feature.render.cache.bossbar;

import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    public BossBarCache() {
        registerVariable("progress", (minecraft, partialTicks) -> {
            var overlay = minecraft.gui.getBossOverlay();
            if (overlay.events.isEmpty()) return Map.of();

            var progress = new HashMap<>();
            for (var entry : overlay.events.entrySet()) {
                progress.put(entry.getKey(), entry.getValue().getProgress());
            }
            return progress;
        });
    }

    @Override
    protected BossBarInformation createCache(Minecraft minecraft, Font font) {
        var overlay = minecraft.gui.getBossOverlay();
        if (overlay.events.isEmpty()) {
            return BossBarInformation.EMPTY;
        }

        // Go through all event overlays and create cache boss bar instances
        Map<UUID, Float> progress = getVariable("progress");
        var bars = new ArrayList<BossBar>();
        for (var entry : overlay.events.entrySet()) {
            var bar = entry.getValue();
            bars.add(new BossBar(
                    new BakedComponent(bar.getName(), font),
                    bar.getOverlay(),
                    bar.getColor(),
                    progress.get(entry.getKey())
            ));
        }
        return new BossBarInformation(bars);
    }

    @Override
    protected boolean shouldForceBlending() {
        // Force consistent blending, to be used for the progress overlay!
        return true;
    }

    @Override
    protected void render(GuiGraphics graphics, BossBarInformation cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font, float partialTicks, boolean dynamic) {
        var currentHeight = HEIGHT;
        for (var bossbar : cache.bars()) {
            // Draw the main bars on the static background
            if (!dynamic) {
                var barLeft = screenWidth / 2 - 91;
                this.drawBar(graphics, barLeft, currentHeight, bossbar, 182, BAR_BACKGROUND_SPRITES, OVERLAY_BACKGROUND_SPRITES);
                var progress = Mth.lerpDiscrete(bossbar.progress(), 0, 182);
                if (progress > 0) {
                    this.drawBar(graphics, barLeft, currentHeight, bossbar, progress, BAR_PROGRESS_SPRITES, OVERLAY_PROGRESS_SPRITES);
                }
            }

            // Draw the text if necessary
            if (bossbar.name().shouldDraw(dynamic)) {
                var x = screenWidth / 2 - bossbar.name().width / 2;
                var y = currentHeight - 9;
                bossbar.name().draw(graphics, font, x, y, 16777215);
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
    private void drawBar(GuiGraphics guiGraphics, int x, int y, BossBar bossBar, int targetWidth, ResourceLocation[] bars, ResourceLocation[] overlays) {
        guiGraphics.blitSprite(bars[bossBar.color().ordinal()], BAR_WIDTH, BAR_HEIGHT, 0, 0, x, y, targetWidth, BAR_HEIGHT);

        // Never draw the progress overlay because progress has no overlay!
        if (bossBar.overlay() != BossEvent.BossBarOverlay.PROGRESS) {
            guiGraphics.blitSprite(overlays[bossBar.overlay().ordinal() - 1], BAR_WIDTH, BAR_HEIGHT, 0, 0, x, y, targetWidth, BAR_HEIGHT);
        }
    }
}
