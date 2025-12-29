package com.noxcrew.noxesium.core.fabric.config;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.util.BooleanOrDefault;
import com.noxcrew.noxesium.core.feature.MapLocation;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import java.util.Arrays;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

/**
 * Stores vanilla option instances for all settings.
 */
public class VanillaOptions {

    public static final OptionInstance<Boolean> RESET_TOGGLE_KEYS = OptionInstance.createBoolean(
            "noxesium.options.reset_toggle_keys.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.reset_toggle_keys.tooltip")),
            NoxesiumMod.getInstance().getConfig().resetToggleKeys,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().resetToggleKeys = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    public static final OptionInstance<BooleanOrDefault> RENDER_MAPS_AS_UI = new OptionInstance<>(
            "noxesium.options.render_maps_as_ui.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.render_maps_as_ui.tooltip")),
            VanillaOptions::enumLabel,
            new OptionInstance.Enum<>(
                    Arrays.asList(BooleanOrDefault.values()),
                    Codec.STRING.xmap(BooleanOrDefault::valueOf, BooleanOrDefault::name)),
            NoxesiumMod.getInstance().getConfig().renderMapsInUi,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().renderMapsInUi = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    public static final OptionInstance<MapLocation> MAP_UI_LOCATION = new OptionInstance<>(
            "noxesium.options.ui_map_location.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.ui_map_location.tooltip")),
            VanillaOptions::enumLabel,
            new OptionInstance.Enum<>(
                    Arrays.asList(MapLocation.TOP, MapLocation.TOP_FLIPPED),
                    Codec.STRING.xmap(MapLocation::valueOf, MapLocation::name)),
            NoxesiumMod.getInstance().getConfig().mapUiLocation,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().mapUiLocation = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static Component enumLabel(Component component, Enum<?> e) {
        return Component.translatable("noxesium.options.enum." + e.name().toLowerCase());
    }
}
