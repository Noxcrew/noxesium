package com.noxcrew.noxesium.paper.feature

import com.noxcrew.noxesium.api.feature.NoxesiumFeature
import com.noxcrew.noxesium.paper.NoxesiumPaper
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

/** A Noxesium feature that is also a Bukkit listener. */
public open class ListeningNoxesiumFeature : NoxesiumFeature(), Listener {
    override fun onRegister() {
        super.onRegister()
        Bukkit.getPluginManager().registerEvents(this, NoxesiumPaper.plugin)
    }

    override fun onUnregister() {
        super.onUnregister()
        HandlerList.unregisterAll(this)
    }
}
