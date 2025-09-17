package com.noxcrew.noxesium.paper.api.event

import com.noxcrew.noxesium.api.component.NoxesiumComponentType
import org.bukkit.entity.Entity
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent

/** Emitted by Noxesium when an entity has its components changed. */
public class NoxesiumEntityComponentChangedEvent<T : Any?>(
    entity: Entity,
    /** The Noxesium player being unregistered. */
    public val component: NoxesiumComponentType<T>,
    /** The old value of the component. */
    public val oldValue: T?,
    /** The new value of the component. */
    public val newValue: T?
) : EntityEvent(entity) {
    public companion object {
        @JvmStatic
        public val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        public fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
