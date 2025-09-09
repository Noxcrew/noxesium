package com.noxcrew.noxesium.legacy

import com.noxcrew.noxesium.paper.EntityRuleIndices.BEAM_COLOR

/**
 * A static location to fetch all entity rule indices.
 */
public object EntityRuleIndices {
    /**
     * If `true` bubbles are removed from guardian beams shot by this entity.
     */
    public const val DISABLE_BUBBLES: Int = 0

    /**
     * Defines a color to use for a beam created by this entity. Applies to guardian beams
     * and end crystal beams.
     */
    public const val BEAM_COLOR: Int = 1

    /**
     * Allows defining qib behavior for an interaction entity. You can find more information
     * about the qib system in the qib package.
     */
    public const val QIB_BEHAVIOR: Int = 2

    /**
     * Allows defining the width of an interaction entity on the Z-axis for the context of
     * qib collisions. The regular width is seen as its width on the X-axis.
     */
    public const val QIB_WIDTH_Z: Int = 3

    /**
     * Defines a color used in combination with [BEAM_COLOR] to create a linear fade.
     */
    public const val BEAM_COLOR_FADE: Int = 4

    /**
     * Defines a custom color to use for glowing by this entity.
     */
    public const val CUSTOM_GLOW_COLOR: Int = 5
}
