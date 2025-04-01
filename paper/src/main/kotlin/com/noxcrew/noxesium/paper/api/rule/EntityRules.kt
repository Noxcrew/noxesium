package com.noxcrew.noxesium.paper.api.rule

import com.noxcrew.noxesium.api.protocol.rule.EntityRuleIndices
import com.noxcrew.noxesium.paper.api.NoxesiumManager
import com.noxcrew.noxesium.paper.api.RuleFunction
import java.awt.Color
import java.util.Optional

/** Stores all known entity rules supported by Noxesium. */
public class EntityRules(
    private val manager: NoxesiumManager,
) {
    /**
     * If `true` bubbles are removed from guardian beams shot by this entity.
     */
    public val disableBubbles: RuleFunction<Boolean> = register(EntityRuleIndices.DISABLE_BUBBLES, 7, ::BooleanServerRule)

    /**
     * Defines a color to use for a beam created by this entity. Applies to guardian beams
     * and end crystal beams.
     */
    public val beamColor: RuleFunction<Optional<Color>> = register(EntityRuleIndices.BEAM_COLOR, 7, ::ColorServerRule)

    /**
     * Allows defining qib behavior for an interaction entity. You can find more information
     * about the qib system in the qib package.
     */
    public val qibBehavior: RuleFunction<String> = register(EntityRuleIndices.QIB_BEHAVIOR, 9) { StringServerRule(it, "") }

    /**
     * Allows defining the width of an interaction entity on the Z-axis for the context of
     * qib collisions. The regular width is seen as its width on the X-axis.
     */
    public val interactionWidthZ: RuleFunction<Double> = register(EntityRuleIndices.QIB_WIDTH_Z, 9) { DoubleServerRule(it, 1.0) }

    /**
     * Defines a color used in combination with [BEAM_COLOR] to create a linear fade.
     */
    public val beamFadeColor: RuleFunction<Optional<Color>> = register(EntityRuleIndices.BEAM_COLOR_FADE, 12, ::ColorServerRule)

    /**
     * Defines a custom color to use for glowing by this entity.
     */
    public val customGlowColor: RuleFunction<Optional<Color>> = register(EntityRuleIndices.CUSTOM_GLOW_COLOR, 16, ::ColorServerRule)

    /** Registers a new [rule]. */
    private fun <T : Any> register(index: Int, minimumProtocol: Int, rule: (Int) -> RemoteServerRule<T>,): RuleFunction<T> {
        val function = RuleFunction(index, rule)
        manager.entityRules.register(index, minimumProtocol, function)
        return function
    }
}
