package com.noxcrew.noxesium.paper.feature.game

import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes
import com.noxcrew.noxesium.paper.component.hasNoxesiumComponent
import com.noxcrew.noxesium.paper.feature.ListeningNoxesiumFeature
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.WindCharge
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

public class ClientAuthoratativeWindcharges : ListeningNoxesiumFeature() {

    //TODO: Dont send the wind charge entity to the player that shot it


    //TODO: Using a deprecated event here because the paper event doesnt provide the source entity only the attacker
    @EventHandler(priority = EventPriority.HIGH)
    public fun onExplosionKnockback(e: org.bukkit.event.entity.EntityKnockbackByEntityEvent) {
        if (e.sourceEntity.type == EntityType.WIND_CHARGE) {
            val owner = (e.sourceEntity as WindCharge).shooter as Player
            if (owner.hasNoxesiumComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_WINDCHARGES)) {
                if (owner.uniqueId == e.entity.uniqueId) {
                    e.isCancelled = true
                }
            }
        }
    }
}