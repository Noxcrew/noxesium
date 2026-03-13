package com.noxcrew.noxesium.paper.commands

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.noxcrew.noxesium.core.feature.EasingType
import com.noxcrew.noxesium.paper.component.noxesiumPlayer
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

/** Creates the `zoom` subcommand. */
public fun zoomCommand(): LiteralArgumentBuilder<CommandSourceStack> = Commands
    .literal("zoom")
    .then(
        Commands
            .argument("targets", ArgumentTypes.players())
            .then(
                Commands
                    .literal("reset")
                    .executes { ctx -> resetZoom(ctx) }
                    .then(
                        Commands
                            .argument("ticks", IntegerArgumentType.integer(0))
                            .executes { ctx -> resetZoom(ctx, IntegerArgumentType.getInteger(ctx, "ticks")) }
                            .also { builder ->
                                for (type in EasingType.entries) {
                                    builder.then(
                                        Commands
                                            .literal(type.name.lowercase())
                                            .executes { ctx -> resetZoom(ctx, IntegerArgumentType.getInteger(ctx, "ticks"), type) },
                                    )
                                }
                            },
                    ),
            ).then(
                Commands
                    .literal("set")
                    .then(
                        Commands
                            .argument("zoom", FloatArgumentType.floatArg())
                            .executes { ctx -> setZoom(ctx) }
                            .then(
                                Commands
                                    .argument("ticks", IntegerArgumentType.integer(0))
                                    .executes { ctx -> setZoom(ctx, IntegerArgumentType.getInteger(ctx, "ticks")) }
                                    .also { builder ->
                                        for (type in EasingType.entries) {
                                            builder.then(
                                                Commands
                                                    .literal(type.name.lowercase())
                                                    .executes { ctx -> setZoom(ctx, IntegerArgumentType.getInteger(ctx, "ticks"), type) }
                                                    .then(
                                                        Commands
                                                            .argument("keep-hand-stationary", BoolArgumentType.bool())
                                                            .executes { ctx ->
                                                                setZoom(
                                                                    ctx,
                                                                    IntegerArgumentType.getInteger(ctx, "ticks"),
                                                                    type,
                                                                    BoolArgumentType.getBool(ctx, "keep-hand-stationary"),
                                                                )
                                                            }.then(
                                                                Commands
                                                                    .argument("fov", IntegerArgumentType.integer())
                                                                    .executes { ctx ->
                                                                        setZoom(
                                                                            ctx,
                                                                            IntegerArgumentType.getInteger(ctx, "ticks"),
                                                                            type,
                                                                            BoolArgumentType.getBool(ctx, "keep-hand-stationary"),
                                                                            IntegerArgumentType.getInteger(ctx, "fov"),
                                                                        )
                                                                    },
                                                            ),
                                                    ),
                                            )
                                        }
                                    },
                            ),
                    ),
            ),
    )

/** Sets the zoom of the target. */
private fun setZoom(
    ctx: CommandContext<CommandSourceStack>,
    transitionTicks: Int = 0,
    easingType: EasingType = EasingType.LINEAR,
    keepHandStationary: Boolean = true,
    fov: Int? = null,
): Int {
    val zoom = ctx.getArgument("zoom", Float::class.java)
    return ctx
        .getArgument("targets", PlayerSelectorArgumentResolver::class.java)
        .resolve(ctx.source)
        .map {
            it.noxesiumPlayer?.also { player ->
                player.setZoom(zoom, transitionTicks, easingType, keepHandStationary, fov)
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
                            "Set zoom for ${it.firstOrNull { it != null }?.username}",
                            NamedTextColor.WHITE,
                        ),
                    )
                }

                else -> {
                    ctx.source.sender.sendMessage(
                        Component.text(
                            "Set zoom for $notNull players",
                            NamedTextColor.WHITE,
                        ),
                    )
                }
            }
            notNull
        }
}

/** Resets the zoom of the target. */
private fun resetZoom(
    ctx: CommandContext<CommandSourceStack>,
    transitionTicks: Int? = null,
    easingType: EasingType = EasingType.LINEAR,
): Int {
    return ctx
        .getArgument("targets", PlayerSelectorArgumentResolver::class.java)
        .resolve(ctx.source)
        .map {
            it.noxesiumPlayer?.also { player ->
                player.resetZoom(transitionTicks, easingType)
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
                            "Reset zoom for ${it.firstOrNull { it != null }?.username}",
                            NamedTextColor.WHITE,
                        ),
                    )
                }

                else -> {
                    ctx.source.sender.sendMessage(
                        Component.text(
                            "Reset zoom for $notNull players",
                            NamedTextColor.WHITE,
                        ),
                    )
                }
            }
            notNull
        }
}
