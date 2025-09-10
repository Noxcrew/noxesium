package com.noxcrew.noxesium.sync.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/** Emitted by Noxesium after it has received a newly updated sync folder from any client. */
public class NoxesiumSyncCompletedEvent(
    /** The id of the sync folder that was updated. */
    public val folderId: String,
) : Event() {
    public companion object {
        @JvmStatic
        public val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        public fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
