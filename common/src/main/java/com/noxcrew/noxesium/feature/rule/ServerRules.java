package com.noxcrew.noxesium.feature.rule;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.api.protocol.rule.ServerRuleIndices;
import com.noxcrew.noxesium.feature.rule.impl.BooleanServerRule;
import com.noxcrew.noxesium.feature.rule.impl.CameraLockedRule;
import com.noxcrew.noxesium.feature.rule.impl.EnableMusicRule;
import com.noxcrew.noxesium.feature.rule.impl.IntegerServerRule;
import com.noxcrew.noxesium.feature.rule.impl.ItemStackListServerRule;
import com.noxcrew.noxesium.feature.rule.impl.ItemStackServerRule;
import com.noxcrew.noxesium.feature.rule.impl.OptionalEnumServerRule;
import com.noxcrew.noxesium.feature.rule.impl.QibBehaviorServerRule;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

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
     * When enabled vanilla background music is fully disabled.
     */
    public static ClientServerRule<Boolean> DISABLE_VANILLA_MUSIC = register(new EnableMusicRule(ServerRuleIndices.DISABLE_VANILLA_MUSIC));

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
     * Allows the server to override the graphics mode used by the client.
     */
    public static OptionalEnumServerRule<GraphicsStatus> OVERRIDE_GRAPHICS_MODE = register(new OptionalEnumServerRule<>(ServerRuleIndices.OVERRIDE_GRAPHICS_MODE, GraphicsStatus.class, Optional.empty(), () -> {
        // We need to call this whenever we change the display type.
        if (Minecraft.getInstance().levelRenderer != null) {
            if (RenderSystem.isOnRenderThread()) {
                Minecraft.getInstance().levelRenderer.allChanged();
            } else {
                RenderSystem.recordRenderCall(() -> {
                    Minecraft.getInstance().levelRenderer.allChanged();
                });
            }
        }
    }));

    /**
     * Enables a custom smoother riptide trident implementation. Requires server-side adjustments.
     */
    public static ClientServerRule<Boolean> ENABLE_SMOOTHER_CLIENT_TRIDENT = register(new BooleanServerRule(ServerRuleIndices.ENABLE_SMOOTHER_CLIENT_TRIDENT, false));

    /**
     * Disables the map showing as a UI element. Can be used to hide it during loading screens.
     */
    public static ClientServerRule<Boolean> DISABLE_MAP_UI = register(new BooleanServerRule(ServerRuleIndices.DISABLE_MAP_UI, false));

    /**
     * Sets the amount of ticks the riptide has coyote time for.
     */
    public static ClientServerRule<Integer> RIPTIDE_COYOTE_TIME = register(new IntegerServerRule(ServerRuleIndices.RIPTIDE_COYOTE_TIME, 5));

    /**
     * Registers a new server rule.
     */
    private static <T extends ClientServerRule<?>> T register(T rule) {
        NoxesiumMod.getInstance().getModule(ServerRuleModule.class).register(rule.getIndex(), rule);
        return rule;
    }
}
