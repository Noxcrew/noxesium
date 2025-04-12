package com.noxcrew.noxesium.api.protocol.rule;

/**
 * A static location to fetch all entity rule indices.
 */
public class EntityRuleIndices {

    /**
     * If `true` bubbles are removed from guardian beams shot by this entity.
     */
    public static final int DISABLE_BUBBLES = 0;

    /**
     * Defines a color to use for a beam created by this entity. Applies to guardian beams
     * and end crystal beams.
     */
    public static final int BEAM_COLOR = 1;

    /**
     * Allows defining qib behavior for an interaction entity. You can find more information
     * about the qib system in the qib package.
     */
    public static final int QIB_BEHAVIOR = 2;

    /**
     * Allows defining the width of an interaction entity on the Z-axis for the context of
     * qib collisions. The regular width is seen as its width on the X-axis.
     */
    public static final int QIB_WIDTH_Z = 3;

    /**
     * Defines a color used in combination with [BEAM_COLOR] to create a linear fade.
     */
    public static final int BEAM_COLOR_FADE = 4;

    /**
     * Defines a custom color to use for glowing by this entity.
     */
    public static final int CUSTOM_GLOW_COLOR = 5;
}
