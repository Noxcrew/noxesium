package com.noxcrew.noxesium.sync

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.noxcrew.noxesium.api.NoxesiumApi
import com.noxcrew.noxesium.paper.commands.noxesiumPlayer
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

/** Creates the `sync` subcommand. */
public fun syncCommand(): LiteralArgumentBuilder<CommandSourceStack> = Commands
    .literal("sync")
    .requires { sender -> sender.sender.hasPermission("noxesium.command.admin") }
    .then(
        Commands
            .argument("folder", StringArgumentType.string())
            .suggests { ctx, builder ->
                NoxesiumApi
                    .getInstance()
                    .getFeatureOrNull(FolderSyncModule::class.java)
                    ?.syncableFolders
                    ?.keys
                    ?.forEach { builder.suggest(it) }
                builder.buildFuture()
            }.executes { ctx ->
                val folder = ctx.getArgument("folder", String::class.java)
                val noxesiumPlayer = ctx.noxesiumPlayer ?: return@executes 0
                noxesiumPlayer.startFolderSync(folder)
                ctx.source.sender.sendMessage(
                    Component.text(
                        "Started syncing folder $folder with ${noxesiumPlayer.username}",
                        NamedTextColor.WHITE,
                    ),
                )
                1
            },
    )
