package com.noxcrew.noxesium.feature.rule;

import com.noxcrew.noxesium.api.protocol.rule.ServerRuleIndices;
import com.noxcrew.noxesium.feature.rule.impl.AdventureModeCheckServerRule;
import com.noxcrew.noxesium.feature.rule.impl.BooleanServerRule;
import com.noxcrew.noxesium.feature.rule.impl.CameraLockedRule;
import com.noxcrew.noxesium.feature.rule.impl.CustomAdventureModeCheck;
import com.noxcrew.noxesium.feature.rule.impl.EnableMusicRule;
import com.noxcrew.noxesium.feature.rule.impl.IntegerServerRule;

/**
 * A class that stores all known server rules.
 */
public class ServerRules {
    /**
     * If `true` disables the riptide spin attack on the trident from colliding with any entities,
     * useful for non-pvp mini-games where the trident is used as a movement tool.
     */
    public static ClientServerRule<Boolean> DISABLE_SPIN_ATTACK_COLLISIONS = new BooleanServerRule(ServerRuleIndices.DISABLE_SPIN_ATTACK_COLLISIONS, false);

    /**
     * A global value for the "CanPlaceOn" item tag that applies to players in adventure mode,
     * allowing the server to define which blocks are placeable globally regardless of tool.
     */
    public static ClientServerRule<CustomAdventureModeCheck> GLOBAL_CAN_PLACE_ON = new AdventureModeCheckServerRule(ServerRuleIndices.GLOBAL_CAN_PLACE_ON);

    /**
     * A global value for the "CanDestroy" item tag that applies to players in adventure mode,
     * allowing the server to define which blocks are breakable globally regardless of tool.
     */
    public static ClientServerRule<CustomAdventureModeCheck> GLOBAL_CAN_DESTROY = new AdventureModeCheckServerRule(ServerRuleIndices.GLOBAL_CAN_DESTROY);

    /**
     * An integer pixel amount to vertically offset the HUD held item name.
     * Useful for avoiding overlapping faction icons. Positive values move the text up.
     */
    public static ClientServerRule<Integer> HELD_ITEM_NAME_OFFSET = new IntegerServerRule(ServerRuleIndices.HELD_ITEM_NAME_OFFSET, 0);

    /**
     * Whether the player should currently prevent any mouse inputs from moving their camera.
     */
    public static ClientServerRule<Boolean> CAMERA_LOCKED = new CameraLockedRule(ServerRuleIndices.CAMERA_LOCKED);

    /**
     * Whether the custom music system should be enabled. When enabled vanilla background music is fully disabled and
     * additional music sliders become available to play music in. This can be used by servers that wish to play their
     * own music.
     */
    public static ClientServerRule<Boolean> ENABLE_CUSTOM_MUSIC = new EnableMusicRule(ServerRuleIndices.ENABLE_CUSTOM_MUSIC);

    /**
     * When true, disables boat collision on the client side, useful for movement games involving
     * boats and other entities in one area. Similar mechanism must exist server side to prevent lagbacks with this enabled.
     */
    public static ClientServerRule<Boolean> DISABLE_BOAT_COLLISIONS = new BooleanServerRule(ServerRuleIndices.DISABLE_BOAT_COLLISIONS, false);


    /**
     * Whether to fully disable drawing numbers in the scorebaord. Even if cancelled out by a shader not drawing
     * the numbers altogether speeds up the rendering process.
     */
    public static ClientServerRule<Boolean> DISABLE_SCOREBOARD_NUMBER_RENDERING = new BooleanServerRule(ServerRuleIndices.DISABLE_SCOREBOARD_NUMBER_RENDERING, true);
}
