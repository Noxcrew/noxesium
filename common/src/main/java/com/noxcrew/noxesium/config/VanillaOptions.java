package com.noxcrew.noxesium.config;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.api.config.BooleanOrDefault;
import com.noxcrew.noxesium.api.config.MapLocation;
import java.util.Arrays;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

/**
 * Stores vanilla option instances for all settings.
 */
public class VanillaOptions {

    private static final OptionInstance<Boolean> resetToggleKeys = OptionInstance.createBoolean(
            "noxesium.options.reset_toggle_keys.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.reset_toggle_keys.tooltip")),
            NoxesiumMod.getInstance().getConfig().resetToggleKeys,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().resetToggleKeys = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<BooleanOrDefault> renderMapsAsUi = new OptionInstance<>(
            "noxesium.options.render_maps_as_ui.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.render_maps_as_ui.tooltip")),
            VanillaOptions::triStateValueLabel,
            new OptionInstance.Enum<>(
                    Arrays.asList(BooleanOrDefault.values()),
                    Codec.STRING.xmap(BooleanOrDefault::valueOf, BooleanOrDefault::name)),
            NoxesiumMod.getInstance().getConfig().renderMapsInUi,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().renderMapsInUi = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<Double> mapUiSize = new OptionInstance<>(
            "noxesium.options.ui_map_size.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.ui_map_size.tooltip")),
            VanillaOptions::percentValueLabel,
            new OptionInstance.IntRange(1, 20).xmap(it -> (double) it / 10.0, it -> (int) (it * 10.0)),
            Codec.doubleRange(0.1, 2.0),
            NoxesiumMod.getInstance().getConfig().mapUiSize,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().mapUiSize = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    private static final OptionInstance<MapLocation> mapUiLocation = new OptionInstance<>(
            "noxesium.options.ui_map_location.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("noxesium.options.ui_map_location.tooltip")),
            VanillaOptions::triStateValueLabel,
            new OptionInstance.Enum<>(
                    Arrays.asList(MapLocation.values()), Codec.STRING.xmap(MapLocation::valueOf, MapLocation::name)),
            NoxesiumMod.getInstance().getConfig().mapUiLocation,
            (newValue) -> {
                NoxesiumMod.getInstance().getConfig().mapUiLocation = newValue;
                NoxesiumMod.getInstance().getConfig().save();
            });

    public static OptionInstance<Boolean> resetToggleKeys() {
        return resetToggleKeys;
    }

    public static OptionInstance<BooleanOrDefault> renderMapsAsUi() {
        return renderMapsAsUi;
    }

    public static OptionInstance<Double> mapUiSize() {
        return mapUiSize;
    }

    public static OptionInstance<MapLocation> mapUiLocation() {
        return mapUiLocation;
    }

    private static Component percentValueLabel(Component component, double d) {
        return Component.translatable("options.percent_value", component, (int) (d * 100.0));
    }

    private static Component triStateValueLabel(Component component, Enum<?> e) {
        return Component.translatable("noxesium.options.enum." + e.name().toLowerCase());
    }
}
