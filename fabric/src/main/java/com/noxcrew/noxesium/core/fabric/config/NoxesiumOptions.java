package com.noxcrew.noxesium.core.fabric.config;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

/**
 * Stores option instances for all settings in the Noxesium mod menu.
 */
public class NoxesiumOptions {

    private static final OptionInstance<Boolean> fpsOverlay = OptionInstance.createBoolean(
            "noxesium.options.fps_overlay.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.fps_overlay.tooltip")),
            NoxesiumMod.getInstance().getConfig().showFpsOverlay,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().showFpsOverlay = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Boolean> gameTimeOverlay = OptionInstance.createBoolean(
            "noxesium.options.game_time_overlay.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.game_time_overlay.tooltip")),
            NoxesiumMod.getInstance().getConfig().showGameTimeOverlay,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().showGameTimeOverlay = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Boolean> dumpIncomingPackets = OptionInstance.createBoolean(
            "noxesium.options.dump_incoming_packets.name",
            OptionInstance.cachedConstantTooltip(
                    Component.translatable("noxesium.options.dump_incoming_packets.tooltip")),
            NoxesiumMod.getInstance().getConfig().dumpIncomingPackets,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().dumpIncomingPackets = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Boolean> dumpOutgoingPackets = OptionInstance.createBoolean(
            "noxesium.options.dump_outgoing_packets.name",
            OptionInstance.cachedConstantTooltip(
                    Component.translatable("noxesium.options.dump_outgoing_packets.tooltip")),
            NoxesiumMod.getInstance().getConfig().dumpOutgoingPackets,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().dumpOutgoingPackets = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Boolean> qibSystemDebugVisuals = OptionInstance.createBoolean(
            "noxesium.options.qib_debug_visuals.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.qib_debug_visuals.tooltip")),
            NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Boolean> extendedPacketLogging = OptionInstance.createBoolean(
            "noxesium.options.extended_packet_logging.name",
            OptionInstance.cachedConstantTooltip(
                    Component.translatable("noxesium.options.extended_packet_logging.tooltip")),
            NoxesiumMod.getInstance().getConfig().printPacketExceptions,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().printPacketExceptions = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Boolean> playerGlowingKeybinds = OptionInstance.createBoolean(
            "noxesium.options.enable_glowing_keybinds.name",
            OptionInstance.cachedConstantTooltip(
                    Component.translatable("noxesium.options.enable_glowing_keybinds.tooltip")),
            NoxesiumMod.getInstance().getConfig().showGlowingSettings,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().showGlowingSettings = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Boolean> debugScoreboardTeams = OptionInstance.createBoolean(
            "noxesium.options.debug_scoreboard_teams.name",
            OptionInstance.cachedConstantTooltip(
                    Component.translatable("noxesium.options.debug_scoreboard_teams.tooltip")),
            NoxesiumMod.getInstance().getConfig().debugScoreboardTeams,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().debugScoreboardTeams = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Boolean> showCullingBoxes = OptionInstance.createBoolean(
            "noxesium.options.show_culling_boxes.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.show_culling_boxes.tooltip")),
            NoxesiumMod.getInstance().getConfig().showCullingBoxes,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().showCullingBoxes = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Double> bossBarPosition = new OptionInstance<>(
            "noxesium.options.boss_bar_position.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.boss_bar_position.tooltip")),
            NoxesiumOptions::percentageLabel,
            new OptionInstance.IntRange(-20, 20).xmap(it -> (double) it / 20.0, it -> (int) (it * 20.0)),
            Codec.doubleRange(-1.0, 1.0),
            NoxesiumMod.getInstance().getConfig().bossBarPosition,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().bossBarPosition = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Double> bossBarScale = new OptionInstance<>(
            "noxesium.options.boss_bar_scale.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.boss_bar_scale.tooltip")),
            NoxesiumOptions::percentageLabel,
            new OptionInstance.IntRange(1, 20).xmap(it -> (double) it / 10.0, it -> (int) (it * 10.0)),
            Codec.doubleRange(0.1, 2.0),
            NoxesiumMod.getInstance().getConfig().bossBarScale,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().bossBarScale = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Double> scoreboardPosition = new OptionInstance<>(
            "noxesium.options.scoreboard_position.name",
            OptionInstance.cachedConstantTooltip(
                    Component.translatable("noxesium.options.scoreboard_position.tooltip")),
            NoxesiumOptions::percentageLabel,
            new OptionInstance.IntRange(-20, 20).xmap(it -> (double) it / 20.0, it -> (int) (it * 20.0)),
            Codec.doubleRange(-1.0, 1.0),
            NoxesiumMod.getInstance().getConfig().scoreboardPosition,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().scoreboardPosition = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Double> scoreboardScale = new OptionInstance<>(
            "noxesium.options.scoreboard_scale.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.scoreboard_scale.tooltip")),
            NoxesiumOptions::percentageLabel,
            new OptionInstance.IntRange(1, 20).xmap(it -> (double) it / 10.0, it -> (int) (it * 10.0)),
            Codec.doubleRange(0.1, 2.0),
            NoxesiumMod.getInstance().getConfig().scoreboardScale,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().scoreboardScale = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Double> tabListScale = new OptionInstance<>(
            "noxesium.options.tab_list_scale.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.tab_list_scale.tooltip")),
            NoxesiumOptions::percentageLabel,
            new OptionInstance.IntRange(1, 20).xmap(it -> (double) it / 10.0, it -> (int) (it * 10.0)),
            Codec.doubleRange(0.1, 2.0),
            NoxesiumMod.getInstance().getConfig().tabListScale,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().tabListScale = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Double> textUiScale = new OptionInstance<>(
            "noxesium.options.text_ui_scale.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.text_ui_scale.tooltip")),
            NoxesiumOptions::percentageLabel,
            new OptionInstance.IntRange(1, 20).xmap(it -> (double) it / 10.0, it -> (int) (it * 10.0)),
            Codec.doubleRange(0.1, 2.0),
            NoxesiumMod.getInstance().getConfig().textUiScale,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().textUiScale = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Double> actionBarScale = new OptionInstance<>(
            "noxesium.options.action_bar_scale.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.action_bar_scale.tooltip")),
            NoxesiumOptions::percentageLabel,
            new OptionInstance.IntRange(1, 20).xmap(it -> (double) it / 10.0, it -> (int) (it * 10.0)),
            Codec.doubleRange(0.1, 2.0),
            NoxesiumMod.getInstance().getConfig().actionBarScale,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().actionBarScale = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    public static OptionInstance<Boolean> fpsOverlay() {
        return fpsOverlay;
    }

    public static OptionInstance<Boolean> gameTimeOverlay() {
        return gameTimeOverlay;
    }

    public static OptionInstance<Boolean> dumpIncomingPackets() {
        return dumpIncomingPackets;
    }

    public static OptionInstance<Boolean> dumpOutgoingPackets() {
        return dumpOutgoingPackets;
    }

    public static OptionInstance<Boolean> qibSystemDebugVisuals() {
        return qibSystemDebugVisuals;
    }

    public static OptionInstance<Boolean> extendedPacketLogging() {
        return extendedPacketLogging;
    }

    public static OptionInstance<Boolean> playerGlowingKeybinds() {
        return playerGlowingKeybinds;
    }

    public static OptionInstance<Boolean> debugScoreboardTeams() {
        return debugScoreboardTeams;
    }

    public static OptionInstance<Boolean> showCullingBoxes() {
        return showCullingBoxes;
    }

    public static OptionInstance<Double> bossBarPosition() {
        return bossBarPosition;
    }

    public static OptionInstance<Double> bossBarScale() {
        return bossBarScale;
    }

    public static OptionInstance<Double> scoreboardPosition() {
        return scoreboardPosition;
    }

    public static OptionInstance<Double> scoreboardScale() {
        return scoreboardScale;
    }

    public static OptionInstance<Double> tabListScale() {
        return tabListScale;
    }

    public static OptionInstance<Double> textUiScale() {
        return textUiScale;
    }

    public static OptionInstance<Double> actionBarScale() {
        return actionBarScale;
    }

    private static Component percentageLabel(Component component, double value) {
        return Component.translatable("options.percent_value", component, (int) (value * 100.0));
    }
}
