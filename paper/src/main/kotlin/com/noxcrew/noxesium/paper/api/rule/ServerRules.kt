package com.noxcrew.noxesium.paper.api.rule

import com.noxcrew.noxesium.api.protocol.rule.ServerRuleIndices
import com.noxcrew.noxesium.api.qib.QibDefinition
import com.noxcrew.noxesium.paper.api.NoxesiumManager
import com.noxcrew.noxesium.paper.api.RuleFunction
import org.bukkit.inventory.ItemStack
import java.util.Optional

/** Stores all known server rules supported by Noxesium. */
public class ServerRules(
    private val manager: NoxesiumManager,
) {
    /**
     * Prevents the riptide trident's spin attack from colliding with any targets.
     */
    public val disableSpinAttackCollisions: RuleFunction<Boolean> =
        register(ServerRuleIndices.DISABLE_SPIN_ATTACK_COLLISIONS, 1, ::BooleanServerRule)

    /**
     * Adds an offset to the action bar text displayed that shows the name
     * of the held item.
     */
    public val heldItemNameOffset: RuleFunction<Int> = register(ServerRuleIndices.HELD_ITEM_NAME_OFFSET, 3, ::IntServerRule)

    /**
     * Prevents the player from moving their camera.
     */
    public val cameraLocked: RuleFunction<Boolean> = register(ServerRuleIndices.CAMERA_LOCKED, 3, ::BooleanServerRule)

    /**
     * Disables vanilla music from playing in the background.
     */
    public val disableVanillaMusic: RuleFunction<Boolean> = register(ServerRuleIndices.DISABLE_VANILLA_MUSIC, 3, ::BooleanServerRule)

    /**
     * Prevents boat on entity collisions on the client side.
     */
    public val disableBoatCollisions: RuleFunction<Boolean> = register(ServerRuleIndices.DISABLE_BOAT_COLLISIONS, 4, ::BooleanServerRule)

    /**
     * Configures an item which will be used whenever the properties of
     * the player's hand are resolved. This applies to adventure mode
     * breaking/placing restrictions as well as tool modifications.
     */
    public val handItemOverride: RuleFunction<ItemStack> = register(ServerRuleIndices.HAND_ITEM_OVERRIDE, 16, ::ItemStackServerRule)

    /**
     * Moves the handheld map to be shown in the top left/right corner instead of
     * in the regular hand slot.
     */
    public val showMapInUi: RuleFunction<Boolean> = register(ServerRuleIndices.SHOW_MAP_IN_UI, 7, ::BooleanServerRule)

    /**
     * Forces the client to run chunk updates immediately instead of deferring
     * them to the off-thread. Can be used to force a client to update the world
     * to avoid de-synchronizations on chunk updates.
     */
    public val disableDeferredChunkUpdates: RuleFunction<Boolean> =
        register(ServerRuleIndices.DISABLE_DEFERRED_CHUNK_UPDATES, 7, ::BooleanServerRule)

    /**
     * Defines a list of items to show in a custom creative tab.
     */
    public val customCreativeItems: RuleFunction<List<ItemStack>> =
        register(ServerRuleIndices.CUSTOM_CREATIVE_ITEMS, 16, ::ItemStackListServerRule)

    /**
     * Defines all known qib behaviors that can be triggered by players interacting with marked interaction entities.
     * These behaviors are defined globally to avoid large amounts of data sending.
     */
    public val qibBehaviors: RuleFunction<Map<String, QibDefinition>> =
        register(ServerRuleIndices.QIB_BEHAVIORS, 9, ::QibBehaviorServerRule)

    /**
     * Allows the server to override the graphics mode used by the client.
     */
    public val overrideGraphicsMode: RuleFunction<Optional<GraphicsType>> =
        register(ServerRuleIndices.OVERRIDE_GRAPHICS_MODE, 8, ::OptionalEnumServerRule)

    /**
     * Enables a custom smoother riptide trident implementation. Requires server-side adjustments.
     */
    public val enableSmootherClientTrident: RuleFunction<Boolean> =
        register(ServerRuleIndices.ENABLE_SMOOTHER_CLIENT_TRIDENT, 9, ::BooleanServerRule)

    /**
     * Disables the map showing as a UI element. Can be used to hide it during loading screens.
     */
    public val disableMapUi: RuleFunction<Boolean> = register(ServerRuleIndices.DISABLE_MAP_UI, 10, ::BooleanServerRule)

    /**
     * Sets the amount of ticks the riptide has coyote time for.
     */
    public val riptideCoyoteTime: RuleFunction<Int> = register(ServerRuleIndices.RIPTIDE_COYOTE_TIME, 10) { IntServerRule(it, 5) }

    /**
     * Enables the ability to pre-charge riptide tridents.
     */
    public val riptidePreCharging: RuleFunction<Boolean> = register(ServerRuleIndices.RIPTIDE_PRE_CHARGING, 13, ::BooleanServerRule)

    /** Registers a new [rule]. */
    private fun <T : Any> register(index: Int, minimumProtocol: Int, rule: (Int) -> RemoteServerRule<T>,): RuleFunction<T> {
        val function = RuleFunction(index, rule)
        manager.serverRules.register(index, minimumProtocol, function)
        return function
    }
}
