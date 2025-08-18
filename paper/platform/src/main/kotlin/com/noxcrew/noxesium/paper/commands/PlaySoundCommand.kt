package com.noxcrew.noxesium.paper.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.core.registries.BuiltInRegistries

/** Creates the `playsound` subcommand. */
public fun playSoundCommand(): LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("playsound")
    .then(
        Commands.argument("sound", ArgumentTypes.key())
            .suggests { ctx, builder ->
                BuiltInRegistries.SOUND_EVENT.map { it.location() }
                    .map { it.toString() }
                    .filter { it.startsWith(builder.remainingLowerCase) }
                    .forEach { builder.suggest(it) }
                builder.buildFuture()
            }
            .executes { ctx ->
                val sound = ctx.getArgument("sound", Key::class.java)
                val player = ctx.noxesiumPlayer
                if (player?.playSound(sound, Sound.Source.MASTER) != null) {
                    ctx.source.sender.sendMessage(
                        Component.text(
                            "Played sound $sound to ${player.username}",
                            NamedTextColor.WHITE,
                        ),
                    )
                    Command.SINGLE_SUCCESS
                } else {
                    0
                }
            }.also {
                for (source in Sound.Source.entries) {
                    it.then(
                        Commands.literal(source.name.lowercase())
                            .executes { ctx ->
                                val sound = ctx.getArgument("sound", Key::class.java)
                                val player = ctx.noxesiumPlayer
                                if (player?.playSound(sound, source) != null) {
                                    ctx.source.sender.sendMessage(
                                        Component.text(
                                            "Played sound $sound to ${player.username}",
                                            NamedTextColor.WHITE,
                                        ),
                                    )
                                    Command.SINGLE_SUCCESS
                                } else {
                                    0
                                }
                            }
                            .then(
                                Commands.argument("targets", ArgumentTypes.players())
                                    .executes { ctx -> playSound(ctx, source) }
                                    .then(
                                        Commands.argument("volume", FloatArgumentType.floatArg(0f))
                                            .executes { ctx ->
                                                playSound(
                                                    ctx,
                                                    source,
                                                    ctx.getArgument("volume", Float::class.java),
                                                )
                                            }
                                            .then(
                                                Commands.argument("pitch", FloatArgumentType.floatArg(0f))
                                                    .executes { ctx ->
                                                        playSound(
                                                            ctx,
                                                            source,
                                                            ctx.getArgument("volume", Float::class.java),
                                                            ctx.getArgument("pitch", Float::class.java),
                                                        )
                                                    }
                                                    .then(
                                                        Commands.argument("offset", FloatArgumentType.floatArg(0f))
                                                            .executes { ctx ->
                                                                playSound(
                                                                    ctx,
                                                                    source,
                                                                    ctx.getArgument("volume", Float::class.java),
                                                                    ctx.getArgument("pitch", Float::class.java),
                                                                    ctx.getArgument("offset", Float::class.java),
                                                                )
                                                            }
                                                            .then(
                                                                Commands.argument("attenuation", BoolArgumentType.bool())
                                                                    .executes { ctx ->
                                                                        playSound(
                                                                            ctx,
                                                                            source,
                                                                            ctx.getArgument("volume", Float::class.java),
                                                                            ctx.getArgument("pitch", Float::class.java),
                                                                            ctx.getArgument("offset", Float::class.java),
                                                                            ctx.getArgument("attenuation", Boolean::class.java),
                                                                        )
                                                                    }
                                                                    .then(
                                                                        Commands.argument("looping", BoolArgumentType.bool())
                                                                            .executes { ctx ->
                                                                                playSound(
                                                                                    ctx,
                                                                                    source,
                                                                                    ctx.getArgument("volume", Float::class.java),
                                                                                    ctx.getArgument("pitch", Float::class.java),
                                                                                    ctx.getArgument("offset", Float::class.java),
                                                                                    ctx.getArgument("attenuation", Boolean::class.java),
                                                                                    ctx.getArgument("looping", Boolean::class.java),
                                                                                )
                                                                            },
                                                                    )
                                                            ),
                                                    ),
                                            ),
                                    ),
                            ),
                    )
                }
            },
    )

/** Plays a sound based on the given context. */
private fun playSound(
    ctx: CommandContext<CommandSourceStack>, source: Sound.Source,
    volume: Float = 1f,
    pitch: Float = 1f,
    offset: Float = 0f,
    attenuation: Boolean = true,
    looping: Boolean = false,
): Int {
    val sound = ctx.getArgument("sound", Key::class.java)
    return ctx.getArgument("targets", PlayerSelectorArgumentResolver::class.java)
        .resolve(ctx.source)
        .map {
            NoxesiumPlayerManager.getInstance().getPlayer(it.uniqueId)?.also { player ->
                player.playSound(
                    sound,
                    source,
                    volume,
                    pitch,
                    offset,
                    looping,
                    attenuation,
                )
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
                            "Played sound $sound to ${it.firstOrNull { it != null }?.username}",
                            NamedTextColor.WHITE,
                        ),
                    )
                }

                else -> {
                    ctx.source.sender.sendMessage(
                        Component.text(
                            "Played sound $sound to $notNull players",
                            NamedTextColor.WHITE,
                        ),
                    )
                }
            }
            notNull
        }
}

/** Determines the Noxesium player for the given context. */
private val CommandContext<CommandSourceStack>.noxesiumPlayer: NoxesiumServerPlayer?
    get() {
        val targetId = source.executor?.uniqueId
        val player = NoxesiumPlayerManager.getInstance().getPlayer(targetId)
        if (player == null) {
            source.sender.sendMessage(Component.text("Target is not a Noxesium user", NamedTextColor.RED))
            return null
        }
        return player
    }
