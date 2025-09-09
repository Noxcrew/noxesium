package com.noxcrew.noxesium.paper

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.noxcrew.noxesium.api.NoxesiumApi
import com.noxcrew.noxesium.api.NoxesiumEntrypoint
import com.noxcrew.noxesium.api.component.NoxesiumEntityManager
import com.noxcrew.noxesium.api.network.NoxesiumNetworking
import com.noxcrew.noxesium.api.nms.NoxesiumPlatform
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.paper.commands.componentCommands
import com.noxcrew.noxesium.paper.commands.listCommand
import com.noxcrew.noxesium.paper.commands.openLinkCommand
import com.noxcrew.noxesium.paper.commands.playSoundCommand
import com.noxcrew.noxesium.paper.component.PaperEntityManager
import com.noxcrew.noxesium.paper.entrypoint.CommonPaperNoxesiumEntrypoint
import com.noxcrew.noxesium.paper.network.PaperNoxesiumClientboundNetworking
import com.noxcrew.noxesium.paper.network.PaperNoxesiumServerHandshaker
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

/**
 * Sets up Noxesium for usage on Paper. Noxesium can be either compiled into your jar or it can
 * be put in the plugins folder as separate plugin. Make sure to initialize this file and run
 * setup() if you compile it into your plugin.
 *
 * Your plugin is required to a Paper plugin if you are embedding it!
 */
public class NoxesiumPaper : JavaPlugin() {
    public companion object {
        /** The main plugin instance to use. */
        public lateinit var plugin: Plugin

        /** Prepares Noxesium's server-side API. */
        public fun prepare(plugin: Plugin, playerManager: NoxesiumPlayerManager = NoxesiumPlayerManager(),) {
            NoxesiumPaper.plugin = plugin
            NoxesiumPlayerManager.setInstance(playerManager)
            NoxesiumPlatform.setInstance(PaperPlatform())
            NoxesiumNetworking.setInstance(PaperNoxesiumClientboundNetworking())
            NoxesiumEntityManager.setInstance(PaperEntityManager())
        }

        /** Enables Noxesium's server-side API. */
        public fun enable(
            entrypoints: Collection<() -> NoxesiumEntrypoint> = emptyList(),
            commands: Collection<() -> LiteralArgumentBuilder<CommandSourceStack>> = emptyList(),
        ) {
            // Process all entry points
            val logger = NoxesiumApi.getLogger()
            val api = NoxesiumApi.getInstance()
            entrypoints.forEach { api.registerAndActivateEntrypoint(it()) }
            logger.info("Loaded ${api.activeEntrypoints.size} Noxesium entrypoints")

            if (commands.isNotEmpty()) {
                // Register the commands, the plugin must be a paper plugin!
                plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commandsLifecycle ->
                    commandsLifecycle.registrar().register(
                        Commands
                            .literal("noxesium")
                            .requires { sender -> sender.sender.hasPermission("noxesium.command") }
                            .apply { commands.forEach { then(it()) } }
                            .build(),
                        "Provides commands for interacting with Noxesium",
                    )
                }
            }

            // Register the handshaking manager
            PaperNoxesiumServerHandshaker().also {
                it.register()

                // Start a ticking loop to check for registry synchronization
                Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
                    it.tick()
                }, 5, 5)
            }
        }
    }

    private val entrypoints = mutableSetOf<() -> NoxesiumEntrypoint>()
    private val commands = mutableSetOf<() -> LiteralArgumentBuilder<CommandSourceStack>>()

    override fun onLoad() {
        prepare(this)

        registerEntrypoint { CommonPaperNoxesiumEntrypoint() }
        registerNoxesiumCommand { listCommand() }
        registerNoxesiumCommand { openLinkCommand() }
        registerNoxesiumCommand { playSoundCommand() }
        registerNoxesiumCommand { componentCommands() }
    }

    override fun onEnable() {
        enable(entrypoints, commands)
    }

    /** Registers a new entrypoint, expects a delayed supplier to avoid constructing the instance early. */
    public fun registerEntrypoint(entrypoint: () -> NoxesiumEntrypoint) {
        entrypoints += entrypoint
    }

    /** Registers a new Noxesium command, expects a delayed value so it can reference registered features. */
    public fun registerNoxesiumCommand(command: () -> LiteralArgumentBuilder<CommandSourceStack>) {
        commands += command
    }
}
