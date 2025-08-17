package com.noxcrew.noxesium.paper.feature

import com.noxcrew.noxesium.core.network.CommonPackets
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents
import net.minecraft.world.item.enchantment.EnchantmentHelper
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.event.CraftEventFactory
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerRiptideEvent

/**
 * Sets up the smooth trident feature which implements trident behavior only
 * on the client, removing any server-side behavior.
 */
public class SmoothTrident : ListeningNoxesiumFeature() {
    private var ignoreCancellation = false

    override fun onRegister() {
        super.onRegister()

        // Listen to the client performing a riptide
        CommonPackets.SERVER_RIPTIDE.addListener(this) { _, packet, playerId ->
            val player = Bukkit.getPlayer(playerId) ?: return@addListener
            val bukkitStack = player.inventory.getItem(packet.slot) ?: return@addListener
            val nmsStack = CraftItemStack.asNMSCopy(bukkitStack) ?: return@addListener
            val serverPlayer = (player as? CraftPlayer)?.handle ?: return@addListener

            val holder =
                EnchantmentHelper
                    .pickHighestLevel(nmsStack, EnchantmentEffectComponents.TRIDENT_SOUND)
                    .orElse(SoundEvents.TRIDENT_THROW)

            // Call the spin attack event, ignore if cancelled!
            ignoreCancellation = true
            CraftEventFactory.callPlayerRiptideEvent(serverPlayer, nmsStack, 0f, 0f, 0f)
            // TODO Return if cancelled
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

    /** Cancel any regular riptide usages. */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public fun onRiptideEvent(e: PlayerRiptideEvent) {
        if (ignoreCancellation) return
        // TODO check if the player has client riptide set

        // TODO e.isCancelled = true
    }

    // TODO Listen to collisions while spin attacking
}