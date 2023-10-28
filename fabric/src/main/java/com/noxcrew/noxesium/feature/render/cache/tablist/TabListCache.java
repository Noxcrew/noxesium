package com.noxcrew.noxesium.feature.render.cache.tablist;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import com.noxcrew.noxesium.feature.render.font.GuiGraphicsExt;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages the current cache of the tab list.
 */
public class TabListCache extends ElementCache<TabListInformation> {

    private static final ResourceLocation PING_UNKNOWN_SPRITE = new ResourceLocation("icon/ping_unknown");
    private static final ResourceLocation PING_1_SPRITE = new ResourceLocation("icon/ping_1");
    private static final ResourceLocation PING_2_SPRITE = new ResourceLocation("icon/ping_2");
    private static final ResourceLocation PING_3_SPRITE = new ResourceLocation("icon/ping_3");
    private static final ResourceLocation PING_4_SPRITE = new ResourceLocation("icon/ping_4");
    private static final ResourceLocation PING_5_SPRITE = new ResourceLocation("icon/ping_5");
    private static final ResourceLocation HEART_CONTAINER_BLINKING_SPRITE = new ResourceLocation("hud/heart/container_blinking");
    private static final ResourceLocation HEART_CONTAINER_SPRITE = new ResourceLocation("hud/heart/container");
    private static final ResourceLocation HEART_FULL_BLINKING_SPRITE = new ResourceLocation("hud/heart/full_blinking");
    private static final ResourceLocation HEART_HALF_BLINKING_SPRITE = new ResourceLocation("hud/heart/half_blinking");
    private static final ResourceLocation HEART_ABSORBING_FULL_BLINKING_SPRITE = new ResourceLocation("hud/heart/absorbing_full_blinking");
    private static final ResourceLocation HEART_FULL_SPRITE = new ResourceLocation("hud/heart/full");
    private static final ResourceLocation HEART_ABSORBING_HALF_BLINKING_SPRITE = new ResourceLocation("hud/heart/absorbing_half_blinking");
    private static final ResourceLocation HEART_HALF_SPRITE = new ResourceLocation("hud/heart/half");
    private static final Comparator<PlayerInfo> PLAYER_COMPARATOR = Comparator
            .<PlayerInfo>comparingInt((player) -> player.getGameMode() == GameType.SPECTATOR ? 1 : 0)
            .thenComparing((player) -> Optionull.mapOrDefault(player.getTeam(), PlayerTeam::getName, ""))
            .thenComparing((player) -> player.getProfile().getName(), String::compareToIgnoreCase);
    private static TabListCache instance;
    private final Map<UUID, TabListInformation.HealthState> healthStates = new HashMap<>();
    private static final int BASE_OFFSET = 10;

    /**
     * Returns the current instance of this tab list cache.
     */
    public static TabListCache getInstance() {
        if (instance == null) {
            instance = new TabListCache();
        }
        return instance;
    }

    /**
     * Returns whether the given objective is relevant to the current cache.
     * We compare against the exact instance of the objective for speed.
     */
    public boolean isObjectiveRelevant(Objective objective) {
        if (cache == null || cache.objective() == null) return false;
        return cache.objective() == objective;
    }

    /**
     * Resets all heart states.
     */
    public void resetHearts() {
        healthStates.clear();
    }

    /**
     * Returns the id of the latency symbol for the given player.
     */
    public ResourceLocation getLatencyBucket(int latency) {
        if (latency < 0) {
            return PING_UNKNOWN_SPRITE;
        } else if (latency < 150) {
            return PING_5_SPRITE;
        } else if (latency < 300) {
            return PING_4_SPRITE;
        } else if (latency < 600) {
            return PING_3_SPRITE;
        } else if (latency < 1000) {
            return PING_2_SPRITE;
        } else {
            return PING_1_SPRITE;
        }
    }

    /**
     * Creates newly cached action bar content information.
     * <p>
     * Depends on the following information:
     * - Current resource pack configuration
     * - The current screen width
     * - The current header/footer of the tab list
     * - The currently shown players
     * - The current latency of all players
     * - The current blinking animations
     */
    @Override
    protected TabListInformation createCache() {
        var minecraft = Minecraft.getInstance();
        var font = minecraft.font;
        var playerTabOverlay = minecraft.gui.getTabList();
        var scoreboard = minecraft.level.getScoreboard();
        var objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);
        var screenWidth = minecraft.getWindow().getGuiScaledWidth();

        var header = playerTabOverlay.header;
        var footer = playerTabOverlay.footer;
        List<PlayerInfo> players = minecraft.player == null ? List.of() : minecraft.player.connection.getListedOnlinePlayers().stream().sorted(PLAYER_COMPARATOR).limit(80L).toList();
        List<UUID> blinking = new ArrayList<>();

        // If there is an objective showing hearts we determine the heart states and
        if (objective != null && objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
            for (var player : players) {
                var score = objective.getScoreboard().getOrCreatePlayerScore(player.getProfile().getName(), objective).getScore();
                var state = healthStates.computeIfAbsent(player.getProfile().getId(), (playerId) -> new TabListInformation.HealthState(score));

                // We store which hearts are blinking so we know which ones are not in the buffer!
                if (!state.isDoneBlinking(minecraft.gui.getGuiTicks())) {
                    blinking.add(player.getProfile().getId());
                }
            }
        }

        // Determine the width
        var maxNameWidth = 0;
        var baseScoreWidth = 0;

        var names = new HashMap<UUID, BakedComponent>();
        for (var playerInfo : players) {
            var name = playerTabOverlay.getNameForDisplay(playerInfo);
            var baked = new BakedComponent(name);
            names.put(playerInfo.getProfile().getId(), baked);
            maxNameWidth = Math.max(maxNameWidth, baked.width);

            if (objective != null && objective.getRenderType() != ObjectiveCriteria.RenderType.HEARTS) {
                var score = scoreboard.getOrCreatePlayerScore(playerInfo.getProfile().getName(), objective);
                baseScoreWidth = Math.max(baseScoreWidth, font.width(" " + score.getScore()));
            }
        }

        // Determine the amount of rows
        var playerCount = players.size();
        var playersPerColumn = playerCount;

        var columns = 1;
        while (playersPerColumn > 20) {
            playersPerColumn = (playerCount + ++columns - 1) / columns;
        }

        // Determine the width of the score part
        var maxScoreWidth = 0;
        if (objective != null) {
            if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
                // Hearts have a fixed width!
                maxScoreWidth = 90;
            } else {
                maxScoreWidth = baseScoreWidth;
            }
        }

        // Determine more about what to draw
        var showSkins = minecraft.isLocalServer() || minecraft.getConnection().getConnection().isEncrypted();
        var columnWidth = Math.min(columns * ((showSkins ? 9 : 0) + maxNameWidth + maxScoreWidth + 13), screenWidth - 50) / columns;
        var left = screenWidth / 2 - (columnWidth * columns + (columns - 1) * 5) / 2;
        var width = columnWidth * columns + (columns - 1) * 5;

        var headerLines = new ArrayList<BakedComponent>();
        var footerLines = new ArrayList<BakedComponent>();

        if (header != null) {
            var lines = font.split(header, screenWidth - 50);
            for (var line : lines) {
                var baked = new BakedComponent(line, font);
                width = Math.max(width, baked.width);
                headerLines.add(baked);
            }
        }
        if (footer != null) {
            var lines = font.split(footer, screenWidth - 50);
            for (var line : lines) {
                var baked = new BakedComponent(line, font);
                width = Math.max(width, baked.width);
                footerLines.add(baked);
            }
        }

        return new TabListInformation(
                headerLines,
                footerLines,
                players,
                blinking,
                names,
                columnWidth,
                maxNameWidth,
                maxScoreWidth,
                width,
                left,
                playersPerColumn,
                showSkins,
                objective
        );
    }

    @Override
    public void renderDirect(GuiGraphics graphics, TabListInformation cache, int screenWidth, int screenHeight, Minecraft minecraft) {
        var scoreboard = minecraft.level.getScoreboard();
        var objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);
        var font = minecraft.font;

        // Update all heart states
        var clearCache = false;
        var players = cache.players();
        if (!healthStates.isEmpty()) {
            var uuids = players.stream().map((playerInfo) -> playerInfo.getProfile().getId()).collect(Collectors.toSet());
            healthStates.keySet().removeIf((uUID) -> !uuids.contains(uUID));

            // If there is an objective showing hearts we determine the heart states and
            if (objective != null && objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
                // Update the remaining heart states, if any have started blinking we need to clear the cache for next tick!
                // This means we can now draw the hearts into the buffer next time. This does mean it starts a tick for
                // any heart blinking to become visible because we need to clear the buffer the tick after.
                for (var player : players) {
                    var score = objective.getScoreboard().getOrCreatePlayerScore(player.getProfile().getName(), objective).getScore();
                    var state = healthStates.computeIfAbsent(player.getProfile().getId(), (playerId) -> new TabListInformation.HealthState(score));
                    if (state.update(score, minecraft.gui.getGuiTicks())) {
                        clearCache = true;
                    }
                }
            }
        }

        // Render the buffer contents
        super.renderDirect(graphics, cache, screenWidth, screenHeight, minecraft);

        graphics.drawManaged(() -> {
            // Render all parts that need to be drawn directly! (blinking hearts)
            var left = cache.left();
            var playersPerColumn = cache.playersPerColumn();
            var columnWidth = cache.columnWidth();
            var height = BASE_OFFSET;

            if (cache.header() != null) {
                for (var line : cache.header()) {
                    if (line.hasObfuscation) {
                        GuiGraphicsExt.drawString(graphics, font, line, screenWidth / 2 - line.width / 2, height, -1, true);
                    }
                    height += font.lineHeight;
                }
                ++height;
            }

            var playerCount = cache.players().size();
            for (int index = 0; index < playerCount; ++index) {
                int z;
                int aa;

                var column = index / playersPerColumn;
                var row = index % playersPerColumn;
                var x = left + column * columnWidth + column * 5;
                var y = height + row * 9;

                if (index >= cache.players().size()) continue;
                var playerInfo = cache.players().get(index);
                var profile = playerInfo.getProfile();

                if (cache.showSkins()) {
                    // Draw the hat layer here! We need to be able to blend.
                    var player = minecraft.level.getPlayerByUUID(profile.getId());
                    var upsideDown = player != null && LivingEntityRenderer.isEntityUpsideDown(player);
                    int l = 8 + (upsideDown ? 8 : 0);
                    int m = 8 * (upsideDown ? -1 : 1);
                    RenderSystem.enableBlend();
                    graphics.blit(playerInfo.getSkin().texture(), x, y, 8, 8, 40.0f, l, 8, m, 64, 64);
                    RenderSystem.disableBlend();
                    x += 9;
                }

                if (objective != null && playerInfo.getGameMode() != GameType.SPECTATOR && (aa = (z = x + cache.maxNameWidth() + 1) + cache.maxScoreWidth()) - z > 5) {
                    // Render the score outside the buffer when in a blinking animation!
                    if (cache.blinking().contains(playerInfo.getProfile().getId())) {
                        this.renderTablistScore(objective, y, profile.getName(), z, aa, profile.getId(), graphics, font);
                    }
                }
            }

            if (cache.footer() != null) {
                height += playersPerColumn * 9 + 1;
                for (var line : cache.footer()) {
                    if (line.hasObfuscation) {
                        GuiGraphicsExt.drawString(graphics, font, line, screenWidth / 2 - line.width / 2, height, -1, true);
                    }
                    height += font.lineHeight;
                }
            }
        });

        // Clear cache at the end as we cannot edit the current cache in this method so we need
        // to expect next tick to sort things out.
        if (clearCache) {
            clearCache();
        }
    }

    @Override
    protected void renderBuffered(GuiGraphics graphics, TabListInformation cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font) {
        var scoreboard = minecraft.level.getScoreboard();
        var objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);

        // Render all parts that need to be drawn directly! (blinking hearts)
        var width = cache.width();
        var left = cache.left();
        var playersPerColumn = cache.playersPerColumn();
        var columnWidth = cache.columnWidth();
        var height = BASE_OFFSET;

        if (cache.header() != null) {
            graphics.fill(screenWidth / 2 - width / 2 - 1, height - 1, screenWidth / 2 + width / 2 + 1, height + cache.header().size() * font.lineHeight, Integer.MIN_VALUE);
            for (var line : cache.header()) {
                if (!line.hasObfuscation) {
                    GuiGraphicsExt.drawString(graphics, font, line, screenWidth / 2 - line.width / 2, height, -1, true);
                }
                height += font.lineHeight;
            }
            ++height;
        }

        graphics.fill(screenWidth / 2 - width / 2 - 1, height - 1, screenWidth / 2 + width / 2 + 1, height + playersPerColumn * 9, Integer.MIN_VALUE);

        var playerCount = cache.players().size();
        var background = minecraft.options.getBackgroundColor(0x20FFFFFF);
        for (int index = 0; index < playerCount; ++index) {
            int z;
            int aa;

            var column = index / playersPerColumn;
            var row = index % playersPerColumn;
            var x = left + column * columnWidth + column * 5;
            var y = height + row * 9;

            graphics.fill(x, y, x + columnWidth, y + 8, background);

            if (index >= cache.players().size()) continue;
            var playerInfo = cache.players().get(index);
            var profile = playerInfo.getProfile();

            if (cache.showSkins()) {
                // Draw only the base layer here!
                var player = minecraft.level.getPlayerByUUID(profile.getId());
                var upsideDown = player != null && LivingEntityRenderer.isEntityUpsideDown(player);
                int l = 8 + (upsideDown ? 8 : 0);
                int m = 8 * (upsideDown ? -1 : 1);
                graphics.blit(playerInfo.getSkin().texture(), x, y, 8, 8, 8.0f, l, 8, m, 64, 64);
                x += 9;
            }

            GuiGraphicsExt.drawString(graphics, font, cache.names().get(playerInfo.getProfile().getId()), x, y, playerInfo.getGameMode() == GameType.SPECTATOR ? -1862270977 : -1, true);
            if (objective != null && playerInfo.getGameMode() != GameType.SPECTATOR && (aa = (z = x + cache.maxNameWidth() + 1) + cache.maxScoreWidth()) - z > 5) {
                if (!cache.blinking().contains(playerInfo.getProfile().getId())) {
                    this.renderTablistScore(objective, y, profile.getName(), z, aa, profile.getId(), graphics, font);
                }
            }
            this.renderPingIcon(graphics, columnWidth, x - (cache.showSkins() ? 9 : 0), y, getLatencyBucket(playerInfo.getLatency()));
        }

        if (cache.footer() != null) {
            graphics.fill(screenWidth / 2 - width / 2 - 1, (height += playersPerColumn * 9 + 1) - 1, screenWidth / 2 + width / 2 + 1, height + cache.footer().size() * font.lineHeight, Integer.MIN_VALUE);
            for (var line : cache.footer()) {
                if (!line.hasObfuscation) {
                    GuiGraphicsExt.drawString(graphics, font, line, screenWidth / 2 - line.width / 2, height, -1, true);
                }
                height += font.lineHeight;
            }
        }
    }

    private void renderPingIcon(GuiGraphics graphics, int x, int offset, int y, ResourceLocation latency) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 100.0F);
        graphics.blitSprite(latency, offset + x - 11, y, 10, 8);
        graphics.pose().popPose();
    }

    private void renderTablistScore(Objective objective, int y, String pUsername, int x, int offset, UUID playerId, GuiGraphics graphics, Font font) {
        var score = objective.getScoreboard().getOrCreatePlayerScore(pUsername, objective).getScore();
        if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
            renderTablistHearts(y, x, offset, playerId, graphics, score, font);
        } else {
            var scoreString = "" + ChatFormatting.YELLOW + score;
            graphics.drawString(font, scoreString, offset - font.width(scoreString), y, 16777215);
        }
    }

    private void renderTablistHearts(int y, int x, int offset, UUID playerId, GuiGraphics graphics, int health, Font font) {
        var healthState = healthStates.computeIfAbsent(playerId, (id) -> new TabListInformation.HealthState(health));
        var guiTicks = Minecraft.getInstance().gui.getGuiTicks();
        var displayedHearts = Mth.positiveCeilDiv(Math.max(health, healthState.displayedValue()), 2);
        var totalHearts = Math.max(health, Math.max(healthState.displayedValue(), 20)) / 2;
        var blinking = healthState.isBlinking(guiTicks);

        if (displayedHearts > 0) {
            var heartOffset = Mth.floor(Math.min((float) (offset - x - 4) / (float) totalHearts, 9.0F));
            int heart;

            if (heartOffset <= 3) {
                var pctHealth = Mth.clamp((float) health / 20.0F, 0.0F, 1.0F);
                heart = (int) ((1.0F - pctHealth) * 255.0F) << 16 | (int) (pctHealth * 255.0F) << 8;
                var hearts = (float) health / 2.0F;

                var text = Component.translatable("multiplayer.player.list.hp", hearts);
                Component trueText;
                if (offset - font.width(text) >= x) {
                    trueText = text;
                } else {
                    trueText = Component.literal("" + hearts);
                }

                graphics.drawString(font, trueText, (offset + x - font.width(trueText)) / 2, y, heart);
            } else {
                var texture = blinking ? HEART_CONTAINER_BLINKING_SPRITE : HEART_CONTAINER_SPRITE;

                for (heart = displayedHearts; heart < totalHearts; ++heart) {
                    graphics.blitSprite(texture, x + heart * heartOffset, y, 9, 9);
                }

                for (heart = 0; heart < displayedHearts; ++heart) {
                    graphics.blitSprite(texture, x + heart * heartOffset, y, 9, 9);
                    if (blinking) {
                        if (heart * 2 + 1 < healthState.displayedValue()) {
                            graphics.blitSprite(HEART_FULL_BLINKING_SPRITE, x + heart * heartOffset, y, 9, 9);
                        }

                        if (heart * 2 + 1 == healthState.displayedValue()) {
                            graphics.blitSprite(HEART_HALF_BLINKING_SPRITE, x + heart * heartOffset, y, 9, 9);
                        }
                    }

                    if (heart * 2 + 1 < health) {
                        graphics.blitSprite(heart >= 10 ? HEART_ABSORBING_FULL_BLINKING_SPRITE : HEART_FULL_SPRITE, x + heart * heartOffset, y, 9, 9);
                    }

                    if (heart * 2 + 1 == health) {
                        graphics.blitSprite(heart >= 10 ? HEART_ABSORBING_HALF_BLINKING_SPRITE : HEART_HALF_SPRITE, x + heart * heartOffset, y, 9, 9);
                    }
                }
            }
        }
    }
}
