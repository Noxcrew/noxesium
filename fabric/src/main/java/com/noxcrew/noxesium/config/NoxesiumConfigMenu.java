package com.noxcrew.noxesium.config;

import com.google.common.collect.ImmutableList;
import com.noxcrew.noxesium.NoxesiumConfig;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpact;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Hooks into Sodium's video settings menu and adds a tab for Noxesium.
 */
public class NoxesiumConfigMenu {

    private static final NoxesiumOptionStorage storage = new NoxesiumOptionStorage();

    public static void configure(List<OptionPage> pages) {
        var groups = ImmutableList.<OptionGroup>builder();
        groups.add(
                OptionGroup.createBuilder()
                        .add(OptionImpl.createBuilder(boolean.class, storage)
                                .setName(Component.translatable("noxesium.options.fps_overlay.name"))
                                .setTooltip(Component.translatable("noxesium.options.fps_overlay.tooltip"))
                                .setImpact(OptionImpact.LOW)
                                .setBinding((config, value) -> config.showFpsOverlay = value, (config) -> config.showFpsOverlay)
                                .setControl(TickBoxControl::new)
                                .build()
                        )
                        .add(OptionImpl.createBuilder(boolean.class, storage)
                                .setName(Component.translatable("noxesium.options.experimental_patches.name"))
                                .setTooltip(Component.translatable("noxesium.options.experimental_patches.tooltip"))
                                .setImpact(OptionImpact.HIGH)
                                .setBinding(NoxesiumConfig::setEnableExperimentalPerformancePatches, NoxesiumConfig::hasConfiguredPerformancePatches)
                                .setControl(TickBoxControl::new)
                                .build()
                        )
                        .build()
        );
        pages.add(new OptionPage(Component.translatable("noxesium.options.pages.noxesium"), groups.build()));
    }
}
