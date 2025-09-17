package com.noxcrew.noxesium.paper.api.event

import org.bukkit.entity.Entity
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent

/** Emitted by Noxesium before an entity has its components cleared. */
public class NoxesiumEntityComponentsClearEvent(entity: Entity) : EntityEvent(entity) {
    public companion object {
        @JvmStatic
        public val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        public fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
