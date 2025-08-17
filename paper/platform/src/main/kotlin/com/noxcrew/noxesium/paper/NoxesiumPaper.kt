package com.noxcrew.noxesium.paper

import com.noxcrew.noxesium.api.NoxesiumApi
import com.noxcrew.noxesium.api.NoxesiumEntrypoint
import com.noxcrew.noxesium.api.network.NoxesiumNetworking
import com.noxcrew.noxesium.api.nms.NoxesiumPlatform
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.core.util.NoxesiumListCommand
import com.noxcrew.noxesium.paper.entrypoint.CommonPaperNoxesiumEntrypoint
import com.noxcrew.noxesium.paper.network.PaperNoxesiumClientboundNetworking
import com.noxcrew.noxesium.paper.network.PaperNoxesiumServerHandshaker
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
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

        // Register /noxlist to show a list of all versions used by Noxesium users, add an extra
        // line at the bottom showing all online players which are not included in the Noxesium
        // list so you can easily see who is not using it.
        getCommand("noxlist")?.setExecutor { sender, command, label, args ->
            val listedPlayers = NoxesiumListCommand.sendUserList(sender)
            val unlistedPlayers = Bukkit.getOnlinePlayers().filter { it.uniqueId !in listedPlayers }
            if (unlistedPlayers.isNotEmpty()) {
                sender.sendMessage(
                    NoxesiumListCommand.formatLine(
                        text("None", NamedTextColor.YELLOW),
                        unlistedPlayers.map {
                            NoxesiumListCommand.PlayerInfo(it.uniqueId, it.displayName(), null, emptyList())
                        },
                    ),
                )
            }
            true
        }
    }
}
