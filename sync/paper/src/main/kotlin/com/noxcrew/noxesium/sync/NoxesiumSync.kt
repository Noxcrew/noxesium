package com.noxcrew.noxesium.sync

import com.noxcrew.noxesium.paper.NoxesiumPaper
import org.bukkit.plugin.java.JavaPlugin

/**
 * Sets up Noxesium Sync, which extends the basic Noxesium plugin.
 */
public class NoxesiumSync : JavaPlugin() {
    override fun onLoad() {
        val noxesiumPaper = getPlugin(NoxesiumPaper::class.java)
        noxesiumPaper.registerEntrypoint { NoxesiumSyncPaperEntrypoint() }
        noxesiumPaper.registerNoxesiumCommand { syncCommand() }
    }
}
