package com.noxcrew.noxesium.config;

import com.noxcrew.noxesium.NoxesiumMod;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

/**
 * Stores vanilla option instances for all settings.
 */
public class VanillaOptions {

    private static final OptionInstance<Boolean> experimentalPatches = OptionInstance.createBoolean(
            "noxesium.options.experimental_patches.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.experimental_patches.tooltip")),
            NoxesiumMod.getInstance().getConfig().hasConfiguredPerformancePatches(),
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().enableExperimentalPerformancePatches = newValue;
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

    public static OptionInstance<Boolean> fpsOverlay() {
        return fpsOverlay;
    }

    public static OptionInstance<Boolean> experimentalPatches() {
        return experimentalPatches;
    }
}
