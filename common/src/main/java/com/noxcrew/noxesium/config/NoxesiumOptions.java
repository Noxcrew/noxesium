package com.noxcrew.noxesium.config;

import com.noxcrew.noxesium.NoxesiumMod;
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
}
