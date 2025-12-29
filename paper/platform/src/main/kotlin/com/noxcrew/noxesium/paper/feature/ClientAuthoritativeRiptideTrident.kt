package com.noxcrew.noxesium.paper.feature

import com.noxcrew.noxesium.core.network.CommonPackets
import com.noxcrew.noxesium.core.network.serverbound.ServerboundRiptidePacket
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes
import io.papermc.paper.event.entity.EntityAttemptSpinAttackEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerPlayerGameMode
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TridentItem
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.GameType
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.event.CraftEventFactory
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRiptideEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * Sets up the client authoritative riptide trident feature which implements trident behavior only
 * on the client, removing any server-side behavior.
 */
public class ClientAuthoritativeRiptideTrident : ListeningNoxesiumFeature() {
    private var ignoreCancellation = false

    init {
        // Listen to the client performing a riptide
        CommonPackets.SERVER_RIPTIDE.addListener(this, ServerboundRiptidePacket::class.java) { reference, packet, playerId ->
            if (!reference.isRegistered) return@addListener
            val player = Bukkit.getPlayer(playerId) ?: return@addListener

            // Ignore non-client authoritative riptide tridents from lying about the tridents!
            if (!player.hasNoxesiumComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_RIPTIDE_TRIDENTS)) return@addListener

            val bukkitStack = player.inventory.getItem(packet.slot) ?: return@addListener
            val nmsStack = CraftItemStack.unwrap(bukkitStack) ?: return@addListener
            val serverPlayer = (player as? CraftPlayer)?.handle ?: return@addListener

            val holder =
                EnchantmentHelper
                    .pickHighestLevel(nmsStack, EnchantmentEffectComponents.TRIDENT_SOUND)
                    .orElse(SoundEvents.TRIDENT_THROW)

            // Call the spin attack event, ignore if cancelled!
            ignoreCancellation = true
            if (!CraftEventFactory.callPlayerRiptideEvent(serverPlayer, nmsStack, 0f, 0f, 0f)) return@addListener
            ignoreCancellation = false

            // Perform the spin attack itself
            serverPlayer.startAutoSpinAttack(20, 8.0F, nmsStack)

            // Play the spin attack noise for other players
            serverPlayer.level().playSound(
                serverPlayer,
                serverPlayer,
                holder.value() as SoundEvent,
                SoundSource.PLAYERS,
                1.0f,
                1.0f,
            )
        }
    }

    /**
     * Override the interaction for pre-chargeable riptide tridents to fully avoid Bukkit so we can modify the check if
     * the trident can be used so it's allowed when not in water.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public fun onPlayerInteract(e: PlayerInteractEvent) {
        if (e.useItemInHand() == Event.Result.DENY) return
        if (!e.player.hasNoxesiumComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_RIPTIDE_TRIDENTS) ||
            !e.player.hasNoxesiumComponent(CommonGameComponentTypes.RIPTIDE_PRE_CHARGING)
        ) {
            return
        }
        if (!e.action.isRightClick) return
        val itemInHand = (e.item as? CraftItemStack)?.handle ?: return
        val player = (e.player as CraftPlayer).handle
        val hand = if (e.hand == EquipmentSlot.OFF_HAND) InteractionHand.OFF_HAND else InteractionHand.MAIN_HAND

        // Ignore non riptide tridents!
        if (itemInHand.item !is TridentItem || EnchantmentHelper.getTridentSpinAttackStrength(itemInHand, player) <= 0f) return

        // Mimic the vanilla use item logic, but we have to modify the is in water or rain check.
        // This runs start using item so we properly start using it even if not in water!
        player.gameMode.modifiedTridentUseItem(player, itemInHand, hand)

        // Prevent the default logic from running as we've replaced it!
        e.setUseItemInHand(Event.Result.DENY)
    }

    /** Custom version of use item modified for tridents. */
    private fun ServerPlayerGameMode.modifiedTridentUseItem(
        player: ServerPlayer,
        stack: ItemStack,
        hand: InteractionHand,
    ): InteractionResult {
        // Logic from ServerPlayerGameMode with irrelevant support for other features that aren't
        // used by tridents removed (most are removed as use duration is not <= 0)
        if (gameModeForPlayer == GameType.SPECTATOR) {
            return InteractionResult.PASS
        } else if (player.cooldowns.isOnCooldown(stack)) {
            return InteractionResult.PASS
        } else {
            // Logic from TridentItem with water check removed
            if (stack.nextDamageWillBreak()) {
                return InteractionResult.FAIL
            } else {
                player.startUsingItem(hand)
                return InteractionResult.CONSUME
            }
        }
    }

    /**
     * Cancel any regular riptide usages so we can trigger it custom with the listener above.
     * This is better as vanilla does the same logic for detecting when a riptide triggers on
     * both client and server which uses the time the item is held and can easily de-sync causing
     * a riptide to only happen on one side. To fix this, we send a packet whenever a riptide
     * occurs and trigger it only based on that.
     *
     * This is also necessary to allow the custom coyote time as otherwise there would be a major
     * de-sync of when the riptide occurs.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public fun onRiptideEvent(e: PlayerRiptideEvent) {
        if (ignoreCancellation) return
        if (e.player.hasNoxesiumComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_RIPTIDE_TRIDENTS)) {
            e.isCancelled = true
        }
    }

    /** Prevent any collisions for clients with collisions disabled. */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public fun onAttemptSpinAttack(e: EntityAttemptSpinAttackEvent) {
        val player = e.entity as? Player ?: return
        if (player.hasNoxesiumComponent(CommonGameComponentTypes.DISABLE_SPIN_ATTACK_COLLISIONS)) {
            e.isCancelled = true
        }
    }
}
