package com.noxcrew.noxesium.paper.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.paper.component.noxesiumPlayer
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.core.registries.BuiltInRegistries
import org.bukkit.entity.Entity
import org.joml.Vector3f

/** Creates the `playsound` subcommand. */
public fun playSoundCommand(): LiteralArgumentBuilder<CommandSourceStack> = Commands
    .literal("playsound")
    .then(
        Commands
            .argument("sound", ArgumentTypes.key())
            .suggests { ctx, builder ->
                BuiltInRegistries.SOUND_EVENT
                    .map { it.location() }
                    .map { it.toString() }
                    .filter { it.startsWith(builder.remainingLowerCase) }
                    .forEach { builder.suggest(it) }
                builder.buildFuture()
            }.executes { ctx ->
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
                    fun <T : ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.addOptionals(
                        entity: ((CommandContext<CommandSourceStack>) -> Entity?) = { null },
                        position: ((CommandContext<CommandSourceStack>) -> Vector3f?) = { null },
                    ): T = executes { ctx ->
                        playSound(
                            ctx,
                            source,
                            entity = entity(ctx),
                            position = position(ctx),
                        )
                    }.then(
                        Commands
                            .argument("volume", FloatArgumentType.floatArg(0f))
                            .executes { ctx ->
                                playSound(
                                    ctx,
                                    source,
                                    ctx.getArgument("volume", Float::class.java),
                                    entity = entity(ctx),
                                    position = position(ctx),
                                )
                            }.then(
                                Commands
                                    .argument("pitch", FloatArgumentType.floatArg(0f))
                                    .executes { ctx ->
                                        playSound(
                                            ctx,
                                            source,
                                            ctx.getArgument("volume", Float::class.java),
                                            ctx.getArgument("pitch", Float::class.java),
                                            entity = entity(ctx),
                                            position = position(ctx),
                                        )
                                    }.then(
                                        Commands
                                            .argument("offset", FloatArgumentType.floatArg(0f))
                                            .executes { ctx ->
                                                playSound(
                                                    ctx,
                                                    source,
                                                    ctx.getArgument("volume", Float::class.java),
                                                    ctx.getArgument("pitch", Float::class.java),
                                                    ctx.getArgument("offset", Float::class.java),
                                                    entity = entity(ctx),
                                                    position = position(ctx),
                                                )
                                            }.then(
                                                Commands
                                                    .argument("attenuation", BoolArgumentType.bool())
                                                    .executes { ctx ->
                                                        playSound(
                                                            ctx,
                                                            source,
                                                            ctx.getArgument("volume", Float::class.java),
                                                            ctx.getArgument("pitch", Float::class.java),
                                                            ctx.getArgument("offset", Float::class.java),
                                                            ctx.getArgument("attenuation", Boolean::class.java),
                                                            entity = entity(ctx),
                                                            position = position(ctx),
                                                        )
                                                    }.then(
                                                        Commands
                                                            .argument("looping", BoolArgumentType.bool())
                                                            .executes { ctx ->
                                                                playSound(
                                                                    ctx,
                                                                    source,
                                                                    ctx.getArgument("volume", Float::class.java),
                                                                    ctx.getArgument("pitch", Float::class.java),
                                                                    ctx.getArgument("offset", Float::class.java),
                                                                    ctx.getArgument("attenuation", Boolean::class.java),
                                                                    ctx.getArgument("looping", Boolean::class.java),
                                                                    entity = entity(ctx),
                                                                    position = position(ctx),
                                                                )
                                                            },
                                                    ),
                                            ),
                                    ),
                            ),
                    )

                    it.then(
                        Commands
                            .literal(source.name.lowercase())
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
                            }.then(
                                Commands
                                    .argument("targets", ArgumentTypes.players())
                                    .executes { ctx -> playSound(ctx, source) }
                                    .then(
                                        Commands
                                            .literal("direct")
                                            .addOptionals(),
                                    ).then(
                                        Commands
                                            .literal("position")
                                            .then(
                                                Commands
                                                    .argument("pos", ArgumentTypes.finePosition())
                                                    .addOptionals(
                                                        position = { ctx ->
                                                            ctx
                                                                .getArgument("pos", FinePositionResolver::class.java)
                                                                .resolve(ctx.source)
                                                                .let { Vector3f(it.x().toFloat(), it.y().toFloat(), it.z().toFloat()) }
                                                        },
                                                    ),
                                            ),
                                    ).then(
                                        Commands
                                            .literal("entity")
                                            .then(
                                                Commands
                                                    .argument("target", ArgumentTypes.entity())
                                                    .addOptionals(
                                                        entity = { ctx ->
                                                            ctx
                                                                .getArgument("target", EntitySelectorArgumentResolver::class.java)
                                                                .resolve(ctx.source)
                                                                .firstOrNull()
                                                        },
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
    ctx: CommandContext<CommandSourceStack>,
    source: Sound.Source,
    volume: Float = 1f,
    pitch: Float = 1f,
    offset: Float = 0f,
    attenuation: Boolean = true,
    looping: Boolean = false,
    entity: Entity? = null,
    position: Vector3f? = null,
): Int {
    val sound = ctx.getArgument("sound", Key::class.java)
    return ctx
        .getArgument("targets", PlayerSelectorArgumentResolver::class.java)
        .resolve(ctx.source)
        .map {
            it.noxesiumPlayer?.also { player ->
                if (entity != null) {
                    player.playSound(
                        entity.entityId,
                        sound,
                        source,
                        volume,
                        pitch,
                        offset,
                        looping,
                        attenuation,
                    )
                } else if (position != null) {
                    player.playSound(
                        position,
                        sound,
                        source,
                        volume,
                        pitch,
                        offset,
                        looping,
                        attenuation,
                    )
                } else {
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
public val CommandContext<CommandSourceStack>.noxesiumPlayer: NoxesiumServerPlayer?
    get() {
        val targetId = source.executor?.uniqueId
        val player = NoxesiumPlayerManager.getInstance().getPlayer(targetId)
        if (player == null) {
            source.sender.sendMessage(Component.text("Target is not a Noxesium user", NamedTextColor.RED))
            return null
        }
        return player
    }
