package com.noxcrew.noxesium.fabric.feature.entity;

import com.noxcrew.noxesium.fabric.NoxesiumMod;
import com.noxcrew.noxesium.fabric.feature.rule.ClientServerRule;
import com.noxcrew.noxesium.fabric.feature.rule.impl.BooleanServerRule;
import com.noxcrew.noxesium.fabric.feature.rule.impl.ColorServerRule;
import com.noxcrew.noxesium.fabric.feature.rule.impl.DoubleServerRule;
import com.noxcrew.noxesium.fabric.feature.rule.impl.StringServerRule;
import java.awt.Color;
import java.util.Optional;

/**
 * A class that stores all types of extra entity data.
 */
public class ExtraEntityData {
    /**
     * If `true` bubbles are removed from guardian beams shot by this entity.
     */
    public static ClientServerRule<Boolean> DISABLE_BUBBLES =
            register(new BooleanServerRule(EntityRuleIndices.DISABLE_BUBBLES, false));

    /**
     * Defines a color to use for a beam created by this entity. Applies to guardian beams
     * and end crystal beams.
     */
    public static ClientServerRule<Optional<Color>> BEAM_COLOR =
            register(new ColorServerRule(EntityRuleIndices.BEAM_COLOR, Optional.empty()));

    /**
     * Allows defining qib behavior for an interaction entity. You can find more information
     * about the qib system in the qib package.
     */
    public static ClientServerRule<String> QIB_BEHAVIOR =
            register(new StringServerRule(EntityRuleIndices.QIB_BEHAVIOR, ""));

    /**
     * Allows defining the width of an interaction entity on the Z-axis for the context of
     * qib collisions. The regular width is seen as its width on the X-axis.
     */
    public static ClientServerRule<Double> QIB_WIDTH_Z =
            register(new DoubleServerRule(EntityRuleIndices.QIB_WIDTH_Z, 1.0));

    /**
     * Defines a color used in combination with [BEAM_COLOR] to create a linear fade.
     */
    public static ClientServerRule<Optional<Color>> BEAM_COLOR_FADE =
            register(new ColorServerRule(EntityRuleIndices.BEAM_COLOR_FADE, Optional.empty()));

    /**
     * Defines a custom color to use for glowing by this entity.
     */
    public static ClientServerRule<Optional<Color>> CUSTOM_GLOW_COLOR =
            register(new ColorServerRule(EntityRuleIndices.CUSTOM_GLOW_COLOR, Optional.empty()));

    /**
     * Registers a new extra entity data key.
     */
    private static <T extends ClientServerRule<?>> T register(T rule) {
        NoxesiumMod.getInstance().getFeature(ExtraEntityDataModule.class).register(rule.getIndex(), rule);
        return rule;
    }
}
