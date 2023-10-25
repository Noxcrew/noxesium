package com.noxcrew.noxesium.feature.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.Minecraft.ON_OSX;

/**
 * Stores information about a single state of the scoreboard. We cache
 * this information as scoreboards are drawn each frame, so storing some
 * information about it between ticks is useful.
 *
 * @param objective    The relevant scoreboard objective.
 * @param players      The players whose team this scoreboard depends on.
 * @param teams        The teams whose properties this scoreboard depends on.
 * @param header       The header text.
 * @param lines        The final lines to be drawn.
 * @param numbers      The text to be drawn for the numbers.
 * @param numberWidths The widths of each row's number.
 * @param headerWidth  The width of the scoreboard's header.
 * @param maxWidth     The maximum width of the scoreboard.
 */
public record CachedScoreboardContents(
        @Nullable
        Objective objective,
        List<String> players,
        List<String> teams,
        BakedComponent header,
        List<BakedComponent> lines,
        List<BakedComponent> numbers,
        List<Integer> numberWidths,
        int headerWidth,
        int maxWidth,
        boolean hasObfuscation
) {

    /**
     * The fallback contents if the scoreboard is empty.
     */
    public static final CachedScoreboardContents EMPTY = new CachedScoreboardContents(null, List.of(), List.of(), new BakedComponent(Component.empty()), List.of(), List.of(), List.of(), 0, 0, false);

    private static CachedScoreboardContents current;
    private static boolean needsRedraw;
    private static ScreenBuffer scoreboardBuffer = null;

    /**
     * Returns the current buffer to use for drawing the scoreboard.
     * The buffer is automatically redrawn if it's not up-to-date.
     */
    public static ScreenBuffer getScoreboardBuffer() {
        RenderSystem.assertOnRenderThread();

        var minecraft = Minecraft.getInstance();
        var font = minecraft.font;
        var screenWidth = minecraft.getWindow().getGuiScaledWidth();
        var screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // Create the buffer and ensure it has the correct size
        if (scoreboardBuffer == null) {
            scoreboardBuffer = new ScreenBuffer();
        }
        if (scoreboardBuffer.resize(screenWidth, screenHeight)) {
            needsRedraw = true;
        }

        // Redraw into the buffer if we have to
        if (needsRedraw) {
            var cache = getCachedScoreboardContents();
            var height = cache.lines().size() * 9;
            var bottom = screenHeight / 2 + height / 3;
            var right = 3;
            var left = screenWidth - cache.maxWidth() - right;
            var backgroundRight = screenWidth - right + 2;
            var background = Minecraft.getInstance().options.getBackgroundColor(0.3f);
            var darkerBackground = Minecraft.getInstance().options.getBackgroundColor(0.4f);

            var buffer = scoreboardBuffer.getTarget();
            try {
                buffer.setClearColor(0f, 0f, 0f, 0f);
                buffer.clear(ON_OSX);
                buffer.bindWrite(true);

                var graphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
                graphics.drawManaged(() -> {
                    // Draw the header
                    var headerTop = bottom - cache.lines().size() * 9;
                    graphics.fill(left - 2, headerTop - 9 - 1, backgroundRight, headerTop - 1, darkerBackground);
                    if (!cache.header.hasObfuscation) {
                        GuiGraphicsExt.drawString(graphics, font, cache.header(), left + cache.maxWidth() / 2 - cache.headerWidth() / 2, headerTop - 9, -1, false);
                    }

                    // Draw the background (vanilla does this per line but we do it once)
                    graphics.fill(left - 2, bottom, backgroundRight, headerTop - 1, background);

                    // Line 1 here is the bottom line, this is because the
                    // finalScores are sorted and we're getting them still
                    // ordered ascending, so we want to display the last
                    // score at the top.
                    for (var line = 1; line <= cache.lines().size(); line++) {
                        var text = cache.lines().get(line - 1);
                        var lineTop = bottom - line * 9;
                        if (!text.hasObfuscation) {
                            GuiGraphicsExt.drawString(graphics, font, text, left, lineTop, -1, false);
                        }

                        if (cache.numbers().size() >= line) {
                            GuiGraphicsExt.drawString(graphics, font, cache.numbers().get(line - 1), backgroundRight - cache.numberWidths().get(line - 1), lineTop, -1, false);
                        }
                    }
                });
            } finally {
                needsRedraw = false;
                buffer.unbindWrite();
                minecraft.getMainRenderTarget().bindWrite(true);
            }
        }
        return scoreboardBuffer;
    }

    /**
     * Clears the currently cached scoreboard contents. The frequency at which this is irrelevant as it's assumed
     * a server, even if it's constantly updating the scoreboard, cannot do so more frequently than a fast enough
     * client would be attempting to render it. Any lowering of scoreboard re-caching from client fps to server
     * update rate is a noticeable improvement.
     * <p>
     * We do tend to only clear the cache if relevant, however we always clear if any changes are made
     * to the scores of players, even if said players' scores are not visible on the scoreboard. This could
     * be optimized further but is deemed unnecessary. If your scoreboard has a lot of hidden players it's
     * probably not one that heavily needs optimization.
     */
    public static void clearCache() {
        current = null;
    }

    /**
     * Returns whether the given player is relevant to the current cache.
     */
    public static boolean isPlayerRelevant(String player) {
        if (current == null) return false;
        return current.players.contains(player);
    }

    /**
     * Returns whether the given team is relevant to the current cache.
     */
    public static boolean isTeamRelevant(String team) {
        if (current == null) return false;
        return current.teams.contains(team);
    }

    /**
     * Returns whether the given objective is relevant to the current cache.
     * We compare against the exact instance of the objective for speed.
     */
    public static boolean isObjectiveRelevant(Objective objective) {
        if (current == null || current.objective == null) return false;
        return current.objective == objective;
    }

    /**
     * Returns the current cached scoreboard contents.
     */
    public static CachedScoreboardContents getCachedScoreboardContents() {
        if (current == null) {
            current = cacheScoreboard();
            needsRedraw = true;
        }
        return current;
    }

    /**
     * Creates newly cached scoreboard content information.
     * Depends on the following information:
     * - Current resource pack configuration
     * - Current scoreboard contents of objective
     * - The draw numbers setting
     * - Any visual properties of the associated teams (team of client player and teams of all players in objective)
     */
    public static CachedScoreboardContents cacheScoreboard() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return CachedScoreboardContents.EMPTY;
        }

        // Determine the currently shown objective
        var scoreboard = player.getScoreboard();
        Objective objective = null;
        var localPlayerTeam = scoreboard.getPlayersTeam(player.getScoreboardName());
        if (localPlayerTeam != null) {
            var displaySlot = DisplaySlot.teamColorToSlot(localPlayerTeam.getColor());
            if (displaySlot != null) {
                objective = scoreboard.getDisplayObjective(displaySlot);
            }
        }
        if (objective == null) {
            objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        }
        if (objective == null) {
            return CachedScoreboardContents.EMPTY;
        }

        var drawNumbers = !ServerRules.DISABLE_SCOREBOARD_NUMBER_RENDERING.getValue();
        var font = Minecraft.getInstance().font;
        var hasObfuscation = false;

        var players = new ArrayList<String>();
        var teams = new ArrayList<String>();
        players.add(player.getScoreboardName());
        if (localPlayerTeam != null) {
            teams.add(localPlayerTeam.getName());
        }

        var scores = (List<Score>) scoreboard.getPlayerScores(objective);
        var finalScores = new ArrayList<BakedComponent>();
        var numberWidths = new ArrayList<Integer>();
        var numbers = new ArrayList<BakedComponent>();

        var title = objective.getDisplayName();
        var lowerLimit = scores.size() - 15;
        var headerWidth = font.width(title);
        var maxWidth = headerWidth;
        var extraWidth = drawNumbers ? font.width(": ") : 0;

        for (var index = scores.size() - 1; index >= lowerLimit && index >= 0; index--) {
            var score = scores.get(index);
            if (score.getOwner() == null || score.getOwner().startsWith("#")) {
                // If we skip a line we can continue iterating further down
                lowerLimit--;
                continue;
            }

            // We are iterating in reverse order, so we add at the start of the list.
            // We want to end up with the last 15 entries of scores.
            var playerTeam = scoreboard.getPlayersTeam(score.getOwner());
            players.add(score.getOwner());
            teams.add(playerTeam.getName());
            var text = PlayerTeam.formatNameForTeam(playerTeam, Component.literal(score.getOwner()));
            var baked = new BakedComponent(text);
            finalScores.add(0, baked);
            if (baked.hasObfuscation) hasObfuscation = true;

            // Update the maximum width we've found, if numbers are being omitted we move the whole
            // background right.
            var number = "" + ChatFormatting.RED + score.getScore();
            var numberWidth = drawNumbers ? font.width(number) : 0;
            maxWidth = Math.max(maxWidth, font.width(text) + extraWidth + numberWidth);
            if (drawNumbers) {
                numberWidths.add(0, numberWidth);
                numbers.add(0, new BakedComponent(Component.literal(String.valueOf(score.getScore())).withStyle(ChatFormatting.RED)));
            }
        }

        var header = new BakedComponent(title);
        return new CachedScoreboardContents(
                objective,
                players,
                teams,
                header,
                finalScores,
                numbers,
                numberWidths,
                headerWidth,
                maxWidth,
                header.hasObfuscation || hasObfuscation
        );
    }
}
