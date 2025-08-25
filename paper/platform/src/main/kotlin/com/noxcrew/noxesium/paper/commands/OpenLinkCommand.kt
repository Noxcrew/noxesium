package com.noxcrew.noxesium.paper.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.noxcrew.noxesium.paper.feature.noxesiumPlayer
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

/** Creates the `link` subcommand. */
public fun openLinkCommand(): LiteralArgumentBuilder<CommandSourceStack> = Commands
    .literal("link")
    .then(
        Commands
            .argument("targets", ArgumentTypes.players())
            .then(
                Commands
                    .argument("url", StringArgumentType.string())
                    .executes { ctx -> openLink(ctx) }
                    .then(
                        Commands
                            .argument("text", ArgumentTypes.component())
                            .executes { ctx -> openLink(ctx, ctx.getArgument("text", Component::class.java)) },
                    ),
            ),
    )

/** Open the given link as a pop-up with extra text. */
private fun openLink(ctx: CommandContext<CommandSourceStack>, text: Component? = null,): Int {
    val url = ctx.getArgument("url", String::class.java)
    return ctx
        .getArgument("targets", PlayerSelectorArgumentResolver::class.java)
        .resolve(ctx.source)
        .map {
            it.noxesiumPlayer?.also { player ->
                player.openLink(url, text)
            }
        }.let {
            val notNull = it.count { it != null }
            when (notNull) {
                0 -> {
                    ctx.source.sender.sendMessage(
                        Component.text(
                            "No targets are Noxesium users",
                            NamedTextColor.RED,
                        ),
                    )
                }

                1 -> {
                    ctx.source.sender.sendMessage(
                        Component.text(
                            "Opened URL dialog for ${it.firstOrNull { it != null }?.username}",
                            NamedTextColor.WHITE,
                        ),
                    )
                }

                else -> {
                    ctx.source.sender.sendMessage(
                        Component.text(
                            "Opened URL dialog for $notNull players",
                            NamedTextColor.WHITE,
                        ),
                    )
                }
            }
            notNull
        }
}
