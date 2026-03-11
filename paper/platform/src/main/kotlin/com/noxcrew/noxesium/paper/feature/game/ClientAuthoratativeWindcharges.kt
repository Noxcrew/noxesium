package com.noxcrew.noxesium.paper.feature.game

import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes
import com.noxcrew.noxesium.paper.component.hasNoxesiumComponent
import com.noxcrew.noxesium.paper.feature.ListeningNoxesiumFeature
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.WindChargeItem
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

public class ClientAuthoratativeWindcharges : ListeningNoxesiumFeature() {

    @EventHandler(priority = EventPriority.HIGH)
    public fun onPlayerInteract(e: PlayerInteractEvent) {
        if (e.useItemInHand() == Event.Result.DENY) return

        if (!e.player.hasNoxesiumComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_WINDCHARGES)) return

        if (!e.action.isRightClick) return
        val itemInHand = (e.item as? CraftItemStack)?.handle ?: return
        val player = (e.player as CraftPlayer).handle
        val hand = if (e.hand == EquipmentSlot.OFF_HAND) InteractionHand.OFF_HAND else InteractionHand.MAIN_HAND

        if (itemInHand.item !is WindChargeItem) return

        //TODO: Replicate vanilla functionality but dont send the entity to the client player

        // Prevent the default logic from running as we've replaced it!
        e.setUseItemInHand(Event.Result.DENY)
    }

}