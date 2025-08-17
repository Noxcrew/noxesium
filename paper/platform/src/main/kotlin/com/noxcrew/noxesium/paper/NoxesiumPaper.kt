package com.noxcrew.noxesium.paper

import com.noxcrew.noxesium.api.NoxesiumApi
import com.noxcrew.noxesium.api.nms.NmsNoxesiumEntrypoint
import com.noxcrew.noxesium.api.nms.NoxesiumNmsApi
import com.noxcrew.noxesium.api.nms.network.NoxesiumNetworking
import com.noxcrew.noxesium.api.nms.network.NoxesiumPlayerManager
import com.noxcrew.noxesium.paper.commands.NoxesiumListCommand
import com.noxcrew.noxesium.paper.entrypoint.CommonPaperNoxesiumEntrypoint
import com.noxcrew.noxesium.paper.network.PaperNoxesiumClientboundNetworking
import com.noxcrew.noxesium.paper.network.PaperNoxesiumServerHandshaker
import net.minecraft.server.level.ServerPlayer
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
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
            extraEntrypoints: List<NmsNoxesiumEntrypoint> = emptyList(),
        ) {
            // Set important instances
            NoxesiumPaper.plugin = plugin
            NoxesiumPlayerManager.setInstance(playerManager)
            NoxesiumNetworking.setInstance(PaperNoxesiumClientboundNetworking())

            // Process all entry points
            val logger = NoxesiumApi.getLogger()
            val api = NoxesiumApi.getInstance()
            api.registerAndActivateEntrypoint(CommonPaperNoxesiumEntrypoint())
            extraEntrypoints.forEach { api.registerAndActivateEntrypoint(it) }

            // Register all packet collections, implementations can do their own packet handling
            // injection or forking of the server-side code to hide packets from entrypoints not
            // known to a player from them.
            val nmsApi = NoxesiumNmsApi.getInstance()
            api.activeEntrypoints
                .filterIsInstance<NmsNoxesiumEntrypoint>()
                .forEach { entrypoint ->
                    entrypoint.packetCollections.forEach { collection -> nmsApi.registerPackets(entrypoint, collection) }
                }
            logger.info("Loaded ${api.activeEntrypoints.size} Noxesium entrypoints")

            // Register the handshaking manager
            PaperNoxesiumServerHandshaker().register()
        }
    }

    override fun onEnable() {
        setup(this)
        getCommand("noxlist")?.setExecutor(NoxesiumListCommand())
    }
}

/** Returns the NMS player instance from a Bukkit player. */
internal val Player.nms: ServerPlayer
    get() = (this as CraftPlayer).handle
