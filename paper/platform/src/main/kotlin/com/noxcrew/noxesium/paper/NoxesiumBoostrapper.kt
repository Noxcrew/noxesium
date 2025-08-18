package com.noxcrew.noxesium.paper

import com.noxcrew.noxesium.paper.commands.listCommand
import com.noxcrew.noxesium.paper.commands.playSoundCommand
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents

/** Sets up Noxesium API commands. */
public class NoxesiumBoostrapper : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        context.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            commands.registrar().register(
                Commands.literal("noxesium")
                    .requires { sender -> sender.sender.hasPermission("noxesium.command") }
                    .then(listCommand())
                    .then(playSoundCommand())
                    .build(),
                "Provides commands for interacting with Noxesium",
            )
        }
    }
}