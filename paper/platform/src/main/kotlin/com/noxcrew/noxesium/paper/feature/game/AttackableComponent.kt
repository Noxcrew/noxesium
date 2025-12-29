package com.noxcrew.noxesium.paper.feature.game

import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes
import com.noxcrew.noxesium.paper.component.getNoxesiumComponent
import com.noxcrew.noxesium.paper.feature.ListeningNoxesiumFeature
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

/**
 * Implements serverside logic to make the attackable component work.
 */
public class AttackableComponent : ListeningNoxesiumFeature() {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public fun onPreAttack(e: PrePlayerAttackEntityEvent) {
        if (e.attacked.getNoxesiumComponent(CommonEntityComponentTypes.ATTACKABLE) == false) {
            e.isCancelled = true
        }
    }
}
