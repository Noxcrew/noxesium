package com.noxcrew.noxesium.paper.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.noxcrew.noxesium.core.util.NoxesiumListCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit

/** Creates the `list` subcommand. */
public fun listCommand(): LiteralArgumentBuilder<CommandSourceStack> = Commands
    .literal("list")
    .executes { ctx ->
        val sender = ctx.source.sender
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
        Command.SINGLE_SUCCESS
    }
