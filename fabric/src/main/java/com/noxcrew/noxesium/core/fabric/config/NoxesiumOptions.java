package com.noxcrew.noxesium.core.fabric.config;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.feature.GuiElement;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

/**
 * Stores option instances for all settings in the Noxesium mod menu.
 */
public class NoxesiumOptions {

    public static final OptionInstance<Boolean> GAME_TIME_OVERLAY = OptionInstance.createBoolean(
            "noxesium.options.game_time_overlay.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.game_time_overlay.tooltip")),
            NoxesiumMod.getInstance().getConfig().showGameTimeOverlay,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().showGameTimeOverlay = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    public static final OptionInstance<Boolean> DUMP_INCOMING_PACKETS = OptionInstance.createBoolean(
            "noxesium.options.dump_incoming_packets.name",
            OptionInstance.cachedConstantTooltip(
                    Component.translatable("noxesium.options.dump_incoming_packets.tooltip")),
            NoxesiumMod.getInstance().getConfig().dumpIncomingPackets,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().dumpIncomingPackets = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    public static final OptionInstance<Boolean> DUMP_OUTGOING_PACKETS = OptionInstance.createBoolean(
            "noxesium.options.dump_outgoing_packets.name",
            OptionInstance.cachedConstantTooltip(
                    Component.translatable("noxesium.options.dump_outgoing_packets.tooltip")),
            NoxesiumMod.getInstance().getConfig().dumpOutgoingPackets,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().dumpOutgoingPackets = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    public static final OptionInstance<Boolean> QIB_SYSTEM_VISUAL_DEBUG = OptionInstance.createBoolean(
            "noxesium.options.qib_debug_visuals.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.qib_debug_visuals.tooltip")),
            NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging = newValue;
                NoxesiumMod.getInstance().getConfig().save();

                // Ensure the renderer turns on if it wasn't already!
                Minecraft.getInstance().debugEntries.rebuildCurrentList();
            });

    public static final OptionInstance<Boolean> EXTENDED_PACKET_LOGGING = OptionInstance.createBoolean(
            "noxesium.options.extended_packet_logging.name",
            OptionInstance.cachedConstantTooltip(
                    Component.translatable("noxesium.options.extended_packet_logging.tooltip")),
            NoxesiumMod.getInstance().getConfig().printPacketExceptions,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().printPacketExceptions = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    public static final OptionInstance<Boolean> DEBUG_SCOREBOARD_TEAMS = OptionInstance.createBoolean(
            "noxesium.options.debug_scoreboard_teams.name",
            OptionInstance.cachedConstantTooltip(
                    Component.translatable("noxesium.options.debug_scoreboard_teams.tooltip")),
            NoxesiumMod.getInstance().getConfig().debugScoreboardTeams,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().debugScoreboardTeams = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    public static final OptionInstance<Boolean> SHOW_CULLING_HITBOXES = OptionInstance.createBoolean(
            "noxesium.options.show_culling_boxes.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.show_culling_boxes.tooltip")),
            NoxesiumMod.getInstance().getConfig().showCullingBoxes,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().showCullingBoxes = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    public static final OptionInstance<Double> BOSS_BAR_POSITION = new OptionInstance<>(
            "noxesium.options.boss_bar_position.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.boss_bar_position.tooltip")),
            NoxesiumOptions::valueLabel,
            new OptionInstance.IntRange(-100, 100).xmap(it -> (double) it / 100.0, it -> (int) (it * 100.0), true),
            Codec.doubleRange(-1.0, 1.0),
            NoxesiumMod.getInstance().getConfig().bossBarPosition,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().bossBarPosition = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    public static final OptionInstance<Double> SCOREBOARD_POSITION = new OptionInstance<>(
            "noxesium.options.scoreboard_position.name",
            OptionInstance.cachedConstantTooltip(
                    Component.translatable("noxesium.options.scoreboard_position.tooltip")),
            NoxesiumOptions::valueLabel,
            new OptionInstance.IntRange(-100, 100).xmap(it -> (double) it / 100.0, it -> (int) (it * 100.0), true),
            Codec.doubleRange(-1.0, 1.0),
            NoxesiumMod.getInstance().getConfig().scoreboardPosition,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().scoreboardPosition = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    public static final OptionInstance<Double> MAP_POSITION = new OptionInstance<>(
            "noxesium.options.map_position.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.map_position.tooltip")),
            NoxesiumOptions::valueLabel,
            new OptionInstance.IntRange(-100, 100).xmap(it -> (double) it / 100.0, it -> (int) (it * 100.0), true),
            Codec.doubleRange(-1.0, 1.0),
            NoxesiumMod.getInstance().getConfig().mapPosition,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().mapPosition = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    public static final Map<GuiElement, OptionInstance<Double>> GUI_SCALES = new LinkedHashMap<>();

    static {
        for (var guiElement : GuiElement.values()) {
            GUI_SCALES.put(
                    guiElement,
                    new OptionInstance<>(
                            "noxesium.options." + guiElement.name().toLowerCase() + "_scale.name",
                            OptionInstance.cachedConstantTooltip(Component.translatable(
                                    "noxesium.options." + guiElement.name().toLowerCase() + "_scale.tooltip")),
                            NoxesiumOptions::percentageLabel,
                            new OptionInstance.IntRange(1, 200)
                                    .xmap(it -> (double) it / 100.0, it -> (int) (it * 100.0), true),
                            Codec.doubleRange(0.01, 2.0),
                            NoxesiumMod.getInstance().getConfig().getScales().getOrDefault(guiElement, 1.0),
                            (newValue) -> {
                                NoxesiumMod.getInstance()
                                        .getConfig()
                                        .getScales()
                                        .put(guiElement, newValue);
                                NoxesiumMod.getInstance().getConfig().save();
                            }));
        }
    }

    private static Component percentageLabel(Component component, double value) {
        return Component.translatable("options.percent_value", component, (int) (value * 100.0));
    }

    private static Component valueLabel(Component component, double value) {
        var integer = (int) (value * 100.0);
        if (integer < 0) {
            return Component.translatable("noxesium.options.simple_value", component, Integer.toString(integer));
        } else {
            return Component.translatable("noxesium.options.simple_value", component, "+" + integer);
        }
    }
}
