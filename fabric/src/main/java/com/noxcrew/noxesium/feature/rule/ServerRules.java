package com.noxcrew.noxesium.feature.rule;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.api.protocol.rule.ServerRuleIndices;
import com.noxcrew.noxesium.feature.rule.impl.BooleanServerRule;
import com.noxcrew.noxesium.feature.rule.impl.CameraLockedRule;
import com.noxcrew.noxesium.feature.rule.impl.EnableMusicRule;
import com.noxcrew.noxesium.feature.rule.impl.IntegerServerRule;
import com.noxcrew.noxesium.feature.rule.impl.ItemStackListServerRule;
import com.noxcrew.noxesium.feature.rule.impl.ItemStackServerRule;
import com.noxcrew.noxesium.feature.rule.impl.QibBehaviorServerRule;
import net.minecraft.world.item.ItemStack;

/**
 * A class that stores all known server rules. Similar to game rules but slightly more powerful
 * for a server to modify.
 */
public class ServerRules {
    /**
     * If `true` disables the riptide spin attack on the trident from colliding with any entities,
     * useful for non-pvp mini-games where the trident is used as a movement tool.
     */
    public static ClientServerRule<Boolean> DISABLE_SPIN_ATTACK_COLLISIONS = register(new BooleanServerRule(ServerRuleIndices.DISABLE_SPIN_ATTACK_COLLISIONS, false));

    /**
     * An integer pixel amount to vertically offset the HUD held item name.
     * Useful for avoiding overlapping faction icons. Positive values move the text up.
     */
    public static ClientServerRule<Integer> HELD_ITEM_NAME_OFFSET = register(new IntegerServerRule(ServerRuleIndices.HELD_ITEM_NAME_OFFSET, 0));

    /**
     * Whether the player should currently prevent any mouse inputs from moving their camera.
     */
    public static ClientServerRule<Boolean> CAMERA_LOCKED = register(new CameraLockedRule(ServerRuleIndices.CAMERA_LOCKED));

    /**
     * Whether custom music is being used. When enabled vanilla background music is fully disabled.
     */
    public static ClientServerRule<Boolean> ENABLE_CUSTOM_MUSIC = register(new EnableMusicRule(ServerRuleIndices.DISABLE_VANILLA_MUSIC));

    /**
     * When true, disables boat collision on the client side, useful for movement games involving
     * boats and other entities in one area. Similar mechanism must exist server side to prevent lagbacks with this enabled.
     */
    public static ClientServerRule<Boolean> DISABLE_BOAT_COLLISIONS = register(new BooleanServerRule(ServerRuleIndices.DISABLE_BOAT_COLLISIONS, false));

    /**
     * Allows overriding the item that is used when resolving the capabilities of an empty item slot.
     */
    public static ClientServerRule<ItemStack> HAND_ITEM_OVERRIDE = register(new ItemStackServerRule(ServerRuleIndices.HAND_ITEM_OVERRIDE));

    /**
     * Allows server to override whether experimental UI optimizations are on.
     */
    public static ClientServerRule<Boolean> DISABLE_UI_OPTIMIZATIONS = register(new BooleanServerRule(ServerRuleIndices.DISABLE_UI_OPTIMIZATIONS, false));

    /**
     * Moves the handheld map to be shown in the top left/right corner instead of
     * in the regular hand slot.
     */
    public static ClientServerRule<Boolean> SHOW_MAP_IN_UI = register(new BooleanServerRule(ServerRuleIndices.SHOW_MAP_IN_UI, false));

    /**
     * Forces the client to run chunk updates immediately instead of deferring
     * them to the off-thread. Can be used to force a client to update the world
     * to avoid de-synchronizations on chunk updates.
     */
    public static ClientServerRule<Boolean> DISABLE_DEFERRED_CHUNK_UPDATES = register(new BooleanServerRule(ServerRuleIndices.DISABLE_DEFERRED_CHUNK_UPDATES, false));

    /**
     * Defines a list of items to show in a custom creative tab.
     */
    public static ItemStackListServerRule CUSTOM_CREATIVE_ITEMS = register(new ItemStackListServerRule(ServerRuleIndices.CUSTOM_CREATIVE_ITEMS));

    /**
     * Defines all known qib behaviors that can be triggered by players interacting with marked interaction entities.
     * These behaviors are defined globally to avoid large amounts of data sending.
     */
    public static QibBehaviorServerRule QIB_BEHAVIORS = register(new QibBehaviorServerRule(ServerRuleIndices.QIB_BEHAVIORS));

    /**
     * Registers a new server rule.
     */
    private static <T extends ClientServerRule<?>> T register(T rule) {
        NoxesiumMod.getInstance().getModule(ServerRuleModule.class).register(rule.getIndex(), rule);
        return rule;
    }
}
