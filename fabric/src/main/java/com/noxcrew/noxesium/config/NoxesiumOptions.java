package com.noxcrew.noxesium.config;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.NoxesiumMod;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

import static net.minecraft.client.Options.genericValueLabel;

/**
 * Stores option instances for all settings in the Noxesium mod menu.
 */
public class NoxesiumOptions {

    private static final OptionInstance<Boolean> experimentalPatches = OptionInstance.createBoolean(
        "noxesium.options.experimental_patches.name",
        OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.experimental_patches.tooltip.v2")),
        NoxesiumMod.getInstance().getConfig().hasConfiguredPerformancePatches(),
        (newValue) -> {
            NoxesiumMod.getInstance().getConfig().disableExperimentalPerformancePatches = !newValue;
            NoxesiumMod.getInstance().getConfig().save();
        }
    );

    private static final OptionInstance<Boolean> fpsOverlay = OptionInstance.createBoolean(
        "noxesium.options.fps_overlay.name",
        OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.fps_overlay.tooltip")),
        NoxesiumMod.getInstance().getConfig().showFpsOverlay,
        (newValue) -> {
            NoxesiumMod.getInstance().getConfig().showFpsOverlay = newValue;
            NoxesiumMod.getInstance().getConfig().save();
        }
    );

    private static final OptionInstance<Boolean> gameTimeOverlay = OptionInstance.createBoolean(
        "noxesium.options.game_time_overlay.name",
        OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.game_time_overlay.tooltip")),
        NoxesiumMod.getInstance().getConfig().showGameTimeOverlay,
        (newValue) -> {
            NoxesiumMod.getInstance().getConfig().showGameTimeOverlay = newValue;
            NoxesiumMod.getInstance().getConfig().save();
        }
    );

    private static final OptionInstance<Boolean> dumpIncomingPackets = OptionInstance.createBoolean(
        "noxesium.options.dump_incoming_packets.name",
        OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.dump_incoming_packets.tooltip")),
        NoxesiumMod.getInstance().getConfig().dumpIncomingPackets,
        (newValue) -> {
            NoxesiumMod.getInstance().getConfig().dumpIncomingPackets = newValue;
            NoxesiumMod.getInstance().getConfig().save();
        }
    );

    private static final OptionInstance<Boolean> dumpOutgoingPackets = OptionInstance.createBoolean(
        "noxesium.options.dump_outgoing_packets.name",
        OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.dump_outgoing_packets.tooltip")),
        NoxesiumMod.getInstance().getConfig().dumpOutgoingPackets,
        (newValue) -> {
            NoxesiumMod.getInstance().getConfig().dumpOutgoingPackets = newValue;
            NoxesiumMod.getInstance().getConfig().save();
        }
    );

    private static final OptionInstance<Boolean> qibSystemDebugVisuals = OptionInstance.createBoolean(
        "noxesium.options.qib_debug_visuals.name",
        OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.qib_debug_visuals.tooltip")),
        NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging,
        (newValue) -> {
            NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging = newValue;
            NoxesiumMod.getInstance().getConfig().save();
        }
    );

    private static final OptionInstance<Boolean> extendedPacketLogging = OptionInstance.createBoolean(
        "noxesium.options.extended_packet_logging.name",
        OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.extended_packet_logging.tooltip")),
        NoxesiumMod.getInstance().getConfig().printPacketExceptions,
        (newValue) -> {
            NoxesiumMod.getInstance().getConfig().printPacketExceptions = newValue;
            NoxesiumMod.getInstance().getConfig().save();
        }
    );

    private static final OptionInstance<Boolean> playerGlowingKeybinds = OptionInstance.createBoolean(
        "noxesium.options.enable_glowing_keybinds.name",
        OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.enable_glowing_keybinds.tooltip")),
        NoxesiumMod.getInstance().getConfig().showGlowingSettings,
        (newValue) -> {
            NoxesiumMod.getInstance().getConfig().showGlowingSettings = newValue;
            NoxesiumMod.getInstance().getConfig().save();
        }
    );

    private static final OptionInstance<Integer> minUiFramerate = new OptionInstance<>(
        "noxesium.options.min_ui_framerate.name",
        OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.min_ui_framerate.tooltip")),
        (text, value) -> value == 260
            ? genericValueLabel(text, Component.translatable("options.framerateLimit.max"))
            : genericValueLabel(text, Component.translatable("options.framerate", value)),
        new OptionInstance.IntRange(1, 26).xmap(it -> it * 10, it -> it / 10),
        Codec.intRange(10, 260),
        NoxesiumMod.getInstance().getConfig().minUiFramerate,
        (newValue) -> {
            NoxesiumMod.getInstance().getConfig().minUiFramerate = newValue;
            NoxesiumMod.getInstance().getConfig().save();
        }
    );

    private static final OptionInstance<Integer> maxUiFramerate = new OptionInstance<>(
        "noxesium.options.max_ui_framerate.name",
        OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.max_ui_framerate.tooltip")),
        (text, value) -> value == 260
            ? genericValueLabel(text, Component.translatable("options.framerateLimit.max"))
            : genericValueLabel(text, Component.translatable("options.framerate", value)),
        new OptionInstance.IntRange(3, 26).xmap(it -> it * 10, it -> it / 10),
        Codec.intRange(30, 260),
        NoxesiumMod.getInstance().getConfig().maxUiFramerate,
        (newValue) -> {
            NoxesiumMod.getInstance().getConfig().maxUiFramerate = newValue;
            NoxesiumMod.getInstance().getConfig().save();
        }
    );

    private static final OptionInstance<Boolean> optimizationOverlay = OptionInstance.createBoolean(
            "noxesium.options.optimization_overlay.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.optimization_overlay.tooltip")),
            NoxesiumMod.getInstance().getConfig().showOptimizationOverlay,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().showOptimizationOverlay = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            }
    );

    public static OptionInstance<Boolean> experimentalPatches() {
        return experimentalPatches;
    }

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

    public static OptionInstance<Integer> minUiFramerate() {
        return minUiFramerate;
    }

    public static OptionInstance<Integer> maxUiFramerate() {
        return maxUiFramerate;
    }

    public static OptionInstance<Boolean> optimizationOverlay() {
        return optimizationOverlay;
    }
}
