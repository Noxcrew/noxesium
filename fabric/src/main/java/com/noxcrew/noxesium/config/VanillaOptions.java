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

    private static final OptionInstance<Boolean> resetToggleKeys = OptionInstance.createBoolean(
            "noxesium.options.reset_toggle_keys.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.reset_toggle_keys.tooltip")),
            NoxesiumMod.getInstance().getConfig().resetToggleKeys,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().resetToggleKeys = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            }
    );

    public static OptionInstance<Boolean> experimentalPatches() {
        return experimentalPatches;
    }

    public static OptionInstance<Boolean> resetToggleKeys() {
        return resetToggleKeys;
    }
}
