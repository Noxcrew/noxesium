package com.noxcrew.noxesium.mixin.performance.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.NoxesiumConfig;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.render.cache.actionbar.ActionBarCache;
import com.noxcrew.noxesium.feature.render.cache.bossbar.BossBarCache;
import com.noxcrew.noxesium.feature.render.cache.chat.ChatCache;
import com.noxcrew.noxesium.feature.render.cache.scoreboard.ScoreboardCache;
import com.noxcrew.noxesium.feature.render.cache.tablist.TabListCache;
import com.noxcrew.noxesium.feature.render.cache.title.TitleCache;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.scores.DisplaySlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    private int screenHeight;

    @Shadow
    private int screenWidth;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private int tickCount;

    @Shadow
    public abstract Font getFont();

    @Shadow
    protected abstract void renderVignette(GuiGraphics guiGraphics, Entity entity);

    @Shadow
    private float scopeScale;

    @Shadow
    @Final
    private static ResourceLocation PUMPKIN_BLUR_LOCATION;

    @Shadow
    protected abstract void renderTextureOverlay(GuiGraphics guiGraphics, ResourceLocation resourceLocation, float f);

    @Shadow
    protected abstract void renderSpyglassOverlay(GuiGraphics guiGraphics, float f);

    @Shadow
    @Final
    private static ResourceLocation POWDER_SNOW_OUTLINE_LOCATION;

    @Shadow
    protected abstract void renderPortalOverlay(GuiGraphics guiGraphics, float f);

    @Shadow
    @Final
    private SpectatorGui spectatorGui;

    @Shadow
    protected abstract void renderHotbar(float f, GuiGraphics guiGraphics);

    @Shadow
    protected abstract void renderCrosshair(GuiGraphics guiGraphics);

    @Shadow
    protected abstract void renderPlayerHealth(GuiGraphics guiGraphics);

    @Shadow
    protected abstract void renderVehicleHealth(GuiGraphics guiGraphics);

    @Shadow
    public abstract void renderJumpMeter(PlayerRideableJumping playerRideableJumping, GuiGraphics guiGraphics, int i);

    @Shadow
    public abstract void renderExperienceBar(GuiGraphics guiGraphics, int i);

    @Shadow
    public abstract void renderSelectedItemName(GuiGraphics guiGraphics);

    @Shadow
    public abstract void renderDemoOverlay(GuiGraphics guiGraphics);

    @Shadow
    protected abstract void renderEffects(GuiGraphics guiGraphics);

    @Shadow
    @Final
    private DebugScreenOverlay debugOverlay;

    @Shadow
    protected abstract void renderSavingIndicator(GuiGraphics guiGraphics);

    @Shadow
    @Final
    private SubtitleOverlay subtitleOverlay;

    /**
     * @author Aeltumn
     * @reason Redo all UI rendering to be much faster
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(GuiGraphics graphics, float partialTicks, CallbackInfo ci) {
        // Override the super-method
        if (NoxesiumMod.getInstance().getConfig().shouldDisableExperimentalPerformancePatches()) return;
        ci.cancel();
        
        /*
            General notes:
            - Profiler calls have been removed as optimizations make individual
              elements not worth tracking on the profiler.
            - Entire method is replaced as any optimizations from other mods just
              get in the way with how thorough Noxesium's optimizations are.
            - These optimizations will stay hidden behind an optional toggle for
              the foreseeable future since they cause a LOT of compatibility issues:
                - Any mod adding a UI element (e.g. AppleSkin) will not function at all
                - Any server using excessive text shaders will not have those work
                - Any animated UI sprites will not animate at all

            The plan is to start solving these compatibility issues over time, possibly
            tearing apart this method override and overriding individual calls to sub-methods.
            That should allow 3rd party mods to still function in their own context to some
            degree. ImmediatelyFast/Exordium take this approach by injecting around the methods
            but that doesn't work here since we replace the entire implementation.

            At least I want to look into detecting if animated sprites are used or if text shaders
            are used that depend on the current time and specifically draw those text strings
            every frame. The goal of this optimization is to make zero compromises though.

            Regardless the whole system causes far too many compatibility issues, so it's all staying
            hidden behind an experimental toggle.
         */

        var window = this.minecraft.getWindow();
        var font = getFont();
        var showGui = !this.minecraft.options.hideGui;

        this.screenWidth = graphics.guiWidth();
        this.screenHeight = graphics.guiHeight();

        // Enable blending, blending is never disabled outside this method!
        RenderSystem.enableBlend();

        // (TODO Optimize) Render the red border effect
        if (Minecraft.useFancyGraphics()) {
            this.renderVignette(graphics, this.minecraft.getCameraEntity());
        } else {
            // TODO Determine why this is here
            RenderSystem.enableDepthTest();
        }

        // (TODO Optimize) Render spyglass / pumpkin overlays
        var deltaFrameTime = this.minecraft.getDeltaFrameTime();
        this.scopeScale = Mth.lerp(0.5F * deltaFrameTime, this.scopeScale, 1.125F);
        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            if (this.minecraft.player.isScoping()) {
                this.renderSpyglassOverlay(graphics, this.scopeScale);
            } else {
                this.scopeScale = 0.5F;
                ItemStack itemstack = this.minecraft.player.getInventory().getArmor(3);
                if (itemstack.is(Blocks.CARVED_PUMPKIN.asItem())) {
                    this.renderTextureOverlay(graphics, PUMPKIN_BLUR_LOCATION, 1.0F);
                }
            }
        }

        // (TODO Optimize) Render frozen overlay
        if (this.minecraft.player.getTicksFrozen() > 0) {
            this.renderTextureOverlay(graphics, POWDER_SNOW_OUTLINE_LOCATION, this.minecraft.player.getPercentFrozen());
        }

        // (TODO Optimize) Render portal overlay
        var portalTicks = Mth.lerp(partialTicks, this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity);
        if (portalTicks > 0.0F && !this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
            this.renderPortalOverlay(graphics, portalTicks);
        }

        // (TODO Optimize) Render the player's hotbar
        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            this.spectatorGui.renderHotbar(graphics);
        } else if (!this.minecraft.options.hideGui) {
            this.renderHotbar(partialTicks, graphics);
        }

        if (showGui) {
            // Vanilla re-enables blend here for safety, presumably for the crosshair as that needs blending on!
            RenderSystem.enableBlend();

            // (TODO Optimize) Render the crosshair and attack indicator
            this.renderCrosshair(graphics);

            // Render the boss bar
            BossBarCache.getInstance().render(graphics, screenWidth, screenHeight, partialTicks, minecraft);

            // (TODO Optimize) Render player's hearts
            if (this.minecraft.gameMode.canHurtPlayer()) {
                this.renderPlayerHealth(graphics);
            }

            // (TODO Optimize) Render vehicle hearts
            this.renderVehicleHealth(graphics);
        }

        // Disable blending so we can draw most non-blending elements.
        // This fixes a vanilla bug with the sleep overlay where pressing F1 changes
        // the brightness of the overlay because vanilla puts this disableBlend in
        // the showGui block.
        RenderSystem.disableBlend();

        if (showGui) {
            // (TODO Optimize) Render the jump meter or experience bar
            var center = this.screenWidth / 2 - 91;
            var playerrideablejumping = this.minecraft.player.jumpableVehicle();
            if (playerrideablejumping != null) {
                this.renderJumpMeter(playerrideablejumping, graphics, center);
            } else if (this.minecraft.gameMode.hasExperience()) {
                this.renderExperienceBar(graphics, center);
            }

            // (TODO Optimize) Render the selected item name or spectator menu tooltip
            if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                this.renderSelectedItemName(graphics);
            } else if (this.minecraft.player.isSpectator()) {
                this.spectatorGui.renderTooltip(graphics);
            }
        }

        // (TODO Optimize) Render the sleeping overlay
        if (this.minecraft.player.getSleepTimer() > 0) {
            this.minecraft.getProfiler().push("sleep");
            var sleepTimer = (float) this.minecraft.player.getSleepTimer();
            var sleepFactor = sleepTimer / 100.0F;
            if (sleepFactor > 1.0F) {
                sleepFactor = 1.0F - (sleepTimer - 100.0F) / 10.0F;
            }

            var color = (int) (220.0F * sleepFactor) << 24 | 1052704;
            graphics.fill(RenderType.guiOverlay(), 0, 0, this.screenWidth, this.screenHeight, color);
            this.minecraft.getProfiler().pop();
        }

        // Render the demo overlay (not optimized since it should never be encountered for Noxesium)
        if (this.minecraft.isDemo()) {
            this.renderDemoOverlay(graphics);
        }

        // (TODO Optimize) Render the potion effects onto the UI as well as the debug screen
        // renderEffects will turn on blend but we do so here for clarity
        RenderSystem.enableBlend();
        this.renderEffects(graphics);

        if (showGui) {
            // (TODO Optimize) Renders the debug screen, vanilla has this outside showGui but
            // showDebugScreen also checks for showGui.
            if (this.debugOverlay.showDebugScreen()) {
                this.debugOverlay.render(graphics);
            }

            // Render the action bar overlay
            ActionBarCache.getInstance().render(graphics, screenWidth, screenHeight, partialTicks, minecraft);

            // Render the title overlay
            TitleCache.getInstance().render(graphics, screenWidth, screenHeight, partialTicks, minecraft);

            // (TODO Optimize) Render the audio subtitles
            this.subtitleOverlay.render(graphics);

            // Render the scoreboard
            ScoreboardCache.getInstance().render(graphics, screenWidth, screenHeight, partialTicks, minecraft);

            // Ensure blending is on
            RenderSystem.enableBlend();

            // Render the chat overlay
            ChatCache.mouseX = Mth.floor(this.minecraft.mouseHandler.xpos() * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth());
            ChatCache.mouseY = Mth.floor(this.minecraft.mouseHandler.ypos() * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight());
            ChatCache.lastTick = this.tickCount;
            ChatCache.getInstance().render(graphics, screenWidth, screenHeight, partialTicks, minecraft);

            // Render the tab list
            var scoreboard = minecraft.level.getScoreboard();
            var objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);
            if (this.minecraft.options.keyPlayerList.isDown() && (!this.minecraft.isLocalServer() || this.minecraft.player.connection.getListedOnlinePlayers().size() >= 1 || objective != null)) {
                TabListCache.getInstance().render(graphics, screenWidth, screenHeight, partialTicks, minecraft);
            }

            // (TODO Optimize) Render the saving indicator
            this.renderSavingIndicator(graphics);

            // (TODO Optimize) Draw the fps counter and overlay
            if (NoxesiumMod.getInstance().getConfig().showFpsOverlay && !this.minecraft.gui.getDebugOverlay().showDebugScreen()) {
                // Draw the current fps
                var text = Component.translatable("debug.fps_overlay", Minecraft.getInstance().getFps());
                var lineOffset = font.lineHeight + 5;
                var offset = FabricLoader.getInstance().isModLoaded("toggle-sprint-display") ? lineOffset : 0;
                graphics.fill(3, 3 + offset, 6 + font.width(text), 6 + font.lineHeight + offset, -1873784752);
                graphics.drawString(font, text, 5, 5 + offset, 0xE0E0E0, false);

                // Draw the state of experimental patches if the keybind is being used
                if (NoxesiumConfig.experimentalPatchesHotkey != null) {
                    var text2 = Component.translatable("debug.noxesium_overlay.on");
                    graphics.fill(3, 3 + offset + lineOffset, 6 + font.width(text2), 6 + font.lineHeight + offset + lineOffset, -1873784752);
                    graphics.drawString(font, text2, 5, 5 + offset + lineOffset, 0xE0E0E0, false);
                }
            }
        }
    }

    @Inject(method = "renderSavingIndicator", at = @At("RETURN"))
    private void injected(GuiGraphics graphics, CallbackInfo ci) {
        var font = minecraft.font;

        // If experimental patches are applied we don't render here
        if (!NoxesiumMod.getInstance().getConfig().shouldDisableExperimentalPerformancePatches()) return;

        // Don't render when the debug screen is shown as it would overlap
        if (NoxesiumMod.getInstance().getConfig().showFpsOverlay && !this.minecraft.gui.getDebugOverlay().showDebugScreen()) {
            // Draw the current fps
            var text = Component.translatable("debug.fps_overlay", Minecraft.getInstance().getFps());
            var lineOffset = font.lineHeight + 5;

            // FIXME Can't just check for a different mod like this that's trash
            var offset = FabricLoader.getInstance().isModLoaded("toggle-sprint-display") ? lineOffset : 0;
            graphics.fill(3, 3 + offset, 6 + font.width(text), 6 + font.lineHeight + offset, -1873784752);
            graphics.drawString(font, text, 5, 5 + offset, 0xE0E0E0, false);

            // Draw the state of experimental patches if they are enabled in the config but disabled with the keybind!
            if (NoxesiumMod.getInstance().getConfig().hasConfiguredPerformancePatches()) {
                var text2 = Component.translatable("debug.noxesium_overlay.off");
                graphics.fill(3, 3 + offset + lineOffset, 6 + font.width(text2), 6 + font.lineHeight + offset + lineOffset, -1873784752);
                graphics.drawString(font, text2, 5, 5 + offset + lineOffset, 0xE0E0E0, false);
            }
        }
    }

    @Inject(method = "setOverlayMessage", at = @At(value = "TAIL"))
    private void setOverlayMessage(Component component, boolean bl, CallbackInfo ci) {
        ActionBarCache.getInstance().clearCache();
    }

    @Inject(method = "resetTitleTimes", at = @At(value = "TAIL"))
    private void resetTitleTimes(CallbackInfo ci) {
        TitleCache.getInstance().clearCache();
    }

    @Inject(method = "setTimes", at = @At(value = "TAIL"))
    private void setTimes(CallbackInfo ci) {
        TitleCache.getInstance().clearCache();
    }

    @Inject(method = "setSubtitle", at = @At(value = "TAIL"))
    private void setSubtitle(CallbackInfo ci) {
        TitleCache.getInstance().clearCache();
    }

    @Inject(method = "setTitle", at = @At(value = "TAIL"))
    private void setTitle(CallbackInfo ci) {
        TitleCache.getInstance().clearCache();
    }

    @Inject(method = "clear", at = @At(value = "TAIL"))
    private void clear(CallbackInfo ci) {
        TitleCache.getInstance().clearCache();
    }
}
