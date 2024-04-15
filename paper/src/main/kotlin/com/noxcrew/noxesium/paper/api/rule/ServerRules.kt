package com.noxcrew.noxesium.paper.api.rule

import com.noxcrew.noxesium.api.protocol.rule.ServerRuleIndices
import com.noxcrew.noxesium.paper.api.NoxesiumManager
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/** An alias for a function that provides a server rule. */
public data class RuleFunction<T : Any>(
    public val index: Int,
    public val function: (Player, Int) -> RemoteServerRule<T>,
)

/** Stores all known server rules supported by Noxesium. */
public class ServerRules(
    public val manager: NoxesiumManager,
) {

    /**
     * Prevents the riptide trident's spin attack from colliding with any targets.
     */
    public val disableSpinAttackCollisions: RuleFunction<Boolean> = register(ServerRuleIndices.DISABLE_SPIN_ATTACK_COLLISIONS) { player, index ->
        BooleanServerRule(player, index)
    }

    /**
     * Adds an offset to the action bar text displayed that shows the name
     * of the held item.
     */
    public val heldItemNameOffset: RuleFunction<Int> = register(ServerRuleIndices.HELD_ITEM_NAME_OFFSET) { player, index ->
        IntServerRule(player, index)
    }

    /**
     * Prevents the player from moving their camera.
     */
    public val cameraLocked: RuleFunction<Boolean> = register(ServerRuleIndices.CAMERA_LOCKED) { player, index ->
        BooleanServerRule(player, index)
    }

    /**
     * Disables vanilla music from playing in the background.
     */
    public val disableVanillaMusic: RuleFunction<Boolean> = register(ServerRuleIndices.DISABLE_VANILLA_MUSIC) { player, index ->
        BooleanServerRule(player, index)
    }

    /**
     * Prevents boat on entity collisions on the client side.
     */
    public val disableBoatCollisions: RuleFunction<Boolean> = register(ServerRuleIndices.DISABLE_BOAT_COLLISIONS) { player, index ->
        BooleanServerRule(player, index)
    }

    /**
     * Configures an item which will be used whenever the properties of
     * the player's hand are resolved. This applies to adventure mode
     * breaking/placing restrictions as well as tool modifications.
     */
    public val handItemOverride: RuleFunction<ItemStack> = register(ServerRuleIndices.HAND_ITEM_OVERRIDE) { player, index ->
        ItemStackServerRule(player, index)
    }

    /** Registers a new [rule]. */
    private fun <T : Any> register(index: Int, rule: (Player, Int) -> RemoteServerRule<T>): RuleFunction<T> {
        val function = RuleFunction(index, rule)
        manager.registerServerRule(index, function)
        return function
    }
}
