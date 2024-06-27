package com.noxcrew.noxesium.feature.entity;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.api.protocol.rule.EntityRuleIndices;
import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import com.noxcrew.noxesium.feature.rule.impl.BooleanServerRule;
import com.noxcrew.noxesium.feature.rule.impl.ColorServerRule;
import com.noxcrew.noxesium.feature.rule.impl.StringServerRule;

import java.awt.Color;
import java.util.Optional;

/**
 * A class that stores all types of extra entity data.
 */
public class ExtraEntityData {
    /**
     * If `true` bubbles are removed from guardian beams shot by this entity.
     */
    public static ClientServerRule<Boolean> DISABLE_BUBBLES = register(new BooleanServerRule(EntityRuleIndices.DISABLE_BUBBLES, false));

    /**
     * If `true` bubbles are removed from guardian beams shot by this entity.
     */
    public static ClientServerRule<Optional<Color>> BEAM_COLOR = register(new ColorServerRule(EntityRuleIndices.BEAM_COLOR, Optional.empty()));

    /**
     * Allows defining qib behavior for an interaction entity. You can find more information
     * about the qib system in the qib package.
     */
    public static ClientServerRule<String> QIB_BEHAVIOR = register(new StringServerRule(EntityRuleIndices.QIB_BEHAVIOR, ""));

    /**
     * Registers a new extra entity data key.
     */
    private static <T extends ClientServerRule<?>> T register(T rule) {
        NoxesiumMod.getInstance().getModule(ExtraEntityDataModule.class).register(rule.getIndex(), rule);
        return rule;
    }
}
