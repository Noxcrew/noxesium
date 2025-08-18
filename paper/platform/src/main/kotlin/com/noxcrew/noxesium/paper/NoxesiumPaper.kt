package com.noxcrew.noxesium.paper

import com.noxcrew.noxesium.api.NoxesiumApi
import com.noxcrew.noxesium.api.NoxesiumEntrypoint
import com.noxcrew.noxesium.api.network.NoxesiumNetworking
import com.noxcrew.noxesium.api.nms.NoxesiumPlatform
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.paper.entrypoint.CommonPaperNoxesiumEntrypoint
import com.noxcrew.noxesium.paper.network.PaperNoxesiumClientboundNetworking
import com.noxcrew.noxesium.paper.network.PaperNoxesiumServerHandshaker
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

/**
 * Sets up Noxesium for usage on Paper. Noxesium can be either compiled into your jar or it can
 * be put in the plugins folder as separate plugin. Make sure to initialize this file and run
 * setup() if you compile it into your plugin.
 */
public class NoxesiumPaper : JavaPlugin() {
    public companion object {
        /** The main plugin instance to use. */
        internal lateinit var plugin: Plugin

        /** Sets up Noxesium's server-side API. */
        public fun setup(
            plugin: Plugin,
            playerManager: NoxesiumPlayerManager = NoxesiumPlayerManager(),
            extraEntrypoints: List<NoxesiumEntrypoint> = emptyList(),
        ) {
            // Set important instances
            NoxesiumPaper.plugin = plugin
            NoxesiumPlayerManager.setInstance(playerManager)
            NoxesiumPlatform.setInstance(PaperPlatform())
            NoxesiumNetworking.setInstance(PaperNoxesiumClientboundNetworking())

            // Process all entry points
            val logger = NoxesiumApi.getLogger()
            val api = NoxesiumApi.getInstance()
            api.registerAndActivateEntrypoint(CommonPaperNoxesiumEntrypoint())
            extraEntrypoints.forEach { api.registerAndActivateEntrypoint(it) }
            logger.info("Loaded ${api.activeEntrypoints.size} Noxesium entrypoints")

            // Register the handshaking manager
            PaperNoxesiumServerHandshaker().register()
        }
    }

    override fun onEnable() {
        setup(this)
    }
}
