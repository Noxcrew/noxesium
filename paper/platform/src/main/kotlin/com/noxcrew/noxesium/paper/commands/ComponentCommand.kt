package com.noxcrew.noxesium.paper.commands

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.noxcrew.noxesium.api.component.NoxesiumComponentType
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry
import com.noxcrew.noxesium.api.util.GraphicsMode
import com.noxcrew.noxesium.api.util.Unit
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes
import com.noxcrew.noxesium.paper.feature.getNoxesiumComponent
import com.noxcrew.noxesium.paper.feature.noxesiumPlayer
import com.noxcrew.noxesium.paper.feature.setNoxesiumComponent
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.minecraft.world.item.ItemStack
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.block.CraftBlock
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.joml.Vector3f
import java.awt.Color
import java.util.Objects

/** Configures a typed value. */
private interface ComponentConfigurer {
    /** Runs the get command for this component type. */
    fun <T> get(ctx: CommandContext<CommandSourceStack>, componentType: NoxesiumComponentType<T>): Int

    /** Rusn the set command for this component type. */
    fun <T> set(ctx: CommandContext<CommandSourceStack>, componentType: NoxesiumComponentType<T>, value: T?): Int
}

/** Creates the component setting subcommands. */
public fun componentCommands(): LiteralArgumentBuilder<CommandSourceStack> = Commands
    .literal("component")
    .then(
        Commands
            .literal("game")
            .then(
                Commands
                    .argument("targets", ArgumentTypes.players())
                    .configureComponentCommand(
                        NoxesiumRegistries.GAME_COMPONENTS,
                        object : ComponentConfigurer {
                            override fun <T> get(ctx: CommandContext<CommandSourceStack>, componentType: NoxesiumComponentType<T>): Int {
                                val player =
                                    ctx
                                        .getArgument("targets", PlayerSelectorArgumentResolver::class.java)
                                        .resolve(ctx.source)
                                        .firstOrNull()
                                        ?.noxesiumPlayer

                                return if (player == null) {
                                    ctx.source.sender.sendMessage(
                                        Component.text(
                                            "Targets is not a Noxesium users",
                                            NamedTextColor.RED,
                                        ),
                                    )
                                    0
                                } else {
                                    val rawValue = player.gameComponents.`noxesium$getComponent`(componentType)
                                    val value =
                                        if (rawValue == null) {
                                            "nothing"
                                        } else {
                                            Objects.toString(rawValue)
                                        }
                                    ctx.source.sender.sendMessage(
                                        Component.text(
                                            "${player.username} has ${componentType.id.asString()} set to $value",
                                            NamedTextColor.RED,
                                        ),
                                    )
                                    1
                                }
                            }

                            override fun <T> set(
                                ctx: CommandContext<CommandSourceStack>,
                                componentType: NoxesiumComponentType<T>,
                                value: T?,
                            ): Int = ctx
                                .getArgument("targets", PlayerSelectorArgumentResolver::class.java)
                                .resolve(ctx.source)
                                .map {
                                    it.noxesiumPlayer?.also { player ->
                                        player.gameComponents.`noxesium$setComponent`(componentType, value)
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
                                                    if (value == null) {
                                                        "Cleared ${componentType.id.asString()} for ${
                                                            it.firstOrNull {
                                                                it != null
                                                            }?.username
                                                        }"
                                                    } else {
                                                        "Set ${componentType.id.asString()} to $value for ${
                                                            it.firstOrNull {
                                                                it != null
                                                            }?.username
                                                        }"
                                                    },
                                                    NamedTextColor.WHITE,
                                                ),
                                            )
                                        }

                                        else -> {
                                            ctx.source.sender.sendMessage(
                                                Component.text(
                                                    if (value == null) {
                                                        "Cleared ${componentType.id.asString()} for $notNull players"
                                                    } else {
                                                        "Set ${componentType.id.asString()} to $value for $notNull players"
                                                    },
                                                    NamedTextColor.WHITE,
                                                ),
                                            )
                                        }
                                    }
                                    notNull
                                }
                        },
                    ),
            ),
    ).then(
        Commands
            .literal("item")
            .configureComponentCommand(
                NoxesiumRegistries.ITEM_COMPONENTS,
                object : ComponentConfigurer {
                    override fun <T> get(ctx: CommandContext<CommandSourceStack>, componentType: NoxesiumComponentType<T>): Int {
                        val executor = ctx.source.executor
                        if (executor !is Player) {
                            ctx.source.sender.sendMessage(
                                Component.text(
                                    "Target is not a player",
                                    NamedTextColor.RED,
                                ),
                            )
                            return 0
                        }
                        val item = executor.inventory.itemInMainHand
                        if (item.isEmpty) {
                            ctx.source.sender.sendMessage(
                                Component.text(
                                    "Target is not holding an item",
                                    NamedTextColor.RED,
                                ),
                            )
                            return 0
                        }

                        val rawValue = item.getNoxesiumComponent(componentType)
                        val value =
                            if (rawValue == null) {
                                "nothing"
                            } else {
                                Objects.toString(rawValue)
                            }
                        ctx.source.sender.sendMessage(
                            Component.join(
                                JoinConfiguration.noSeparators(),
                                listOf(
                                    item.effectiveName(),
                                    Component.text(
                                        " has ${componentType.id.asString()} set to $value",
                                        NamedTextColor.WHITE,
                                    ),
                                ),
                            ),
                        )
                        return 1
                    }

                    override fun <T> set(
                        ctx: CommandContext<CommandSourceStack>,
                        componentType: NoxesiumComponentType<T>,
                        value: T?,
                    ): Int {
                        val executor = ctx.source.executor
                        if (executor !is Player) {
                            ctx.source.sender.sendMessage(
                                Component.text(
                                    "Target is not a player",
                                    NamedTextColor.RED,
                                ),
                            )
                            return 0
                        }
                        val item = executor.inventory.itemInMainHand
                        if (item.isEmpty) {
                            ctx.source.sender.sendMessage(
                                Component.text(
                                    "Target is not holding an item",
                                    NamedTextColor.RED,
                                ),
                            )
                            return 0
                        }

                        item.setNoxesiumComponent(componentType, value)
                        ctx.source.sender.sendMessage(
                            Component.join(
                                JoinConfiguration.noSeparators(),
                                listOf(
                                    Component.text(
                                        if (value == null) {
                                            "Cleared ${componentType.id.asString()} on "
                                        } else {
                                            "Set ${componentType.id.asString()} to $value on "
                                        },
                                        NamedTextColor.WHITE,
                                    ),
                                    item.effectiveName(),
                                ),
                            ),
                        )
                        return 1
                    }
                },
            ),
    ).then(
        Commands
            .literal("entity")
            .then(
                Commands
                    .argument("targets", ArgumentTypes.entities())
                    .configureComponentCommand(
                        NoxesiumRegistries.ENTITY_COMPONENTS,
                        object : ComponentConfigurer {
                            override fun <T> get(ctx: CommandContext<CommandSourceStack>, componentType: NoxesiumComponentType<T>): Int {
                                val entity =
                                    ctx
                                        .getArgument("targets", EntitySelectorArgumentResolver::class.java)
                                        .resolve(ctx.source)
                                        .firstOrNull()

                                return if (entity == null) {
                                    ctx.source.sender.sendMessage(
                                        Component.text(
                                            "Entity not found",
                                            NamedTextColor.RED,
                                        ),
                                    )
                                    0
                                } else {
                                    val rawValue = entity.getNoxesiumComponent(componentType)
                                    val value =
                                        if (rawValue == null) {
                                            "nothing"
                                        } else {
                                            Objects.toString(rawValue)
                                        }
                                    ctx.source.sender.sendMessage(
                                        Component.join(
                                            JoinConfiguration.noSeparators(),
                                            listOf(
                                                entity.name(),
                                                Component.text(
                                                    " has ${componentType.id.asString()} set to $value",
                                                    NamedTextColor.RED,
                                                ),
                                            ),
                                        ),
                                    )
                                    1
                                }
                            }

                            override fun <T> set(
                                ctx: CommandContext<CommandSourceStack>,
                                componentType: NoxesiumComponentType<T>,
                                value: T?,
                            ): Int = ctx
                                .getArgument("targets", EntitySelectorArgumentResolver::class.java)
                                .resolve(ctx.source)
                                .onEach {
                                    it.setNoxesiumComponent(componentType, value)
                                }.let {
                                    val notNull = it.count { it != null }
                                    when (notNull) {
                                        0 -> {
                                            ctx.source.sender.sendMessage(
                                                Component.text(
                                                    "No entities selected",
                                                    NamedTextColor.RED,
                                                ),
                                            )
                                        }

                                        1 -> {
                                            ctx.source.sender.sendMessage(
                                                Component.join(
                                                    JoinConfiguration.noSeparators(),
                                                    listOf(
                                                        Component.text(
                                                            if (value == null) {
                                                                "Cleared ${componentType.id.asString()} for "
                                                            } else {
                                                                "Set ${componentType.id.asString()} to $value for "
                                                            },
                                                            NamedTextColor.WHITE,
                                                        ),
                                                        it.first().name(),
                                                    ),
                                                ),
                                            )
                                        }

                                        else -> {
                                            ctx.source.sender.sendMessage(
                                                Component.text(
                                                    if (value == null) {
                                                        "Cleared ${componentType.id.asString()} for $notNull entities"
                                                    } else {
                                                        "Set ${componentType.id.asString()} to $value for $notNull entities"
                                                    },
                                                    NamedTextColor.WHITE,
                                                ),
                                            )
                                        }
                                    }
                                    notNull
                                }
                        },
                    ),
            ),
    ).then(
        Commands
            .literal("block-entity")
            .then(
                Commands
                    .argument("target", ArgumentTypes.blockPosition())
                    .configureComponentCommand(
                        NoxesiumRegistries.BLOCK_ENTITY_COMPONENTS,
                        object : ComponentConfigurer {
                            override fun <T> get(ctx: CommandContext<CommandSourceStack>, componentType: NoxesiumComponentType<T>): Int {
                                val resolver = ctx.getArgument("target", BlockPositionResolver::class.java)
                                val blockPosition = resolver.resolve(ctx.source)
                                val block =
                                    ctx.source.location.world.getBlockAt(
                                        blockPosition.blockX(),
                                        blockPosition.blockY(),
                                        blockPosition.blockZ(),
                                    )
                                val blockEntity =
                                    (ctx.source.location.world as CraftWorld).handle.getBlockEntity((block as CraftBlock).position)
                                if (blockEntity == null) {
                                    ctx.source.sender.sendMessage(
                                        Component.text(
                                            "Target block is not a block entity",
                                            NamedTextColor.RED,
                                        ),
                                    )
                                    return 0
                                }

                                val rawValue = blockEntity.getNoxesiumComponent(componentType)
                                val value =
                                    if (rawValue == null) {
                                        "nothing"
                                    } else {
                                        Objects.toString(rawValue)
                                    }

                                ctx.source.sender.sendMessage(
                                    Component.text(
                                        "${blockPosition.blockX()}, ${blockPosition.blockY()}, ${blockPosition.blockZ()} has ${componentType.id.asString()} set to $value",
                                        NamedTextColor.WHITE,
                                    ),
                                )
                                return 1
                            }

                            override fun <T> set(
                                ctx: CommandContext<CommandSourceStack>,
                                componentType: NoxesiumComponentType<T>,
                                value: T?,
                            ): Int {
                                val resolver = ctx.getArgument("target", BlockPositionResolver::class.java)
                                val blockPosition = resolver.resolve(ctx.source)
                                val block =
                                    ctx.source.location.world.getBlockAt(
                                        blockPosition.blockX(),
                                        blockPosition.blockY(),
                                        blockPosition.blockZ(),
                                    )
                                val blockEntity =
                                    (ctx.source.location.world as CraftWorld).handle.getBlockEntity((block as CraftBlock).position)
                                if (blockEntity == null) {
                                    ctx.source.sender.sendMessage(
                                        Component.text(
                                            "Target block is not a block entity",
                                            NamedTextColor.RED,
                                        ),
                                    )
                                    return 0
                                }

                                blockEntity.setNoxesiumComponent(componentType, value)
                                ctx.source.sender.sendMessage(
                                    Component.text(
                                        if (value == null) {
                                            "Cleared ${componentType.id.asString()} on ${blockPosition.blockX()}, ${blockPosition.blockY()}, ${blockPosition.blockZ()}"
                                        } else {
                                            "Set ${componentType.id.asString()} to $value on ${blockPosition.blockX()}, ${blockPosition.blockY()}, ${blockPosition.blockZ()}"
                                        },
                                        NamedTextColor.WHITE,
                                    ),
                                )
                                return 1
                            }
                        },
                    ),
            ),
    )

private fun <T : ArgumentBuilder<CommandSourceStack, *>> T.configureComponentCommand(
    registry: NoxesiumRegistry<NoxesiumComponentType<*>>,
    configurer: ComponentConfigurer,
): T {
    val validIds = mutableSetOf<NoxesiumComponentType<*>>()
    then(
        Commands.literal("set").also { branch ->
            for (type in registry.contents) {
                fun <T : Enum<T>> forEnum(clazz: Class<T>) {
                    // Enums are special and use literals instead!
                    branch.then(
                        Commands
                            .literal(type.id.asString())
                            .apply {
                                for (value in clazz.enumConstants) {
                                    then(
                                        Commands
                                            .literal(value.name.lowercase())
                                            .executes { ctx ->
                                                configurer.set(ctx, type as NoxesiumComponentType<T>, value as T)
                                            },
                                    )
                                }
                            },
                    )
                    validIds += type
                }

                val argumentBuilder =
                    when {
                        // Hard-code the restrict debug options which is an integer list, we cannot
                        // otherwise properly support lists of options.
                        type == CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS ->
                            Commands
                                .argument("value", StringArgumentType.string())
                                .executes { ctx ->
                                    configurer.set(
                                        ctx,
                                        type as NoxesiumComponentType<List<Int>>,
                                        ctx.getArgument("value", String::class.java).split(",").mapNotNull { it.toIntOrNull() },
                                    )
                                }

                        String::class.java.isAssignableFrom(type.clazz) ->
                            Commands
                                .argument("value", StringArgumentType.string())
                                .executes { ctx ->
                                    configurer.set(
                                        ctx,
                                        type as NoxesiumComponentType<String>,
                                        ctx.getArgument("value", String::class.java),
                                    )
                                }

                        java.lang.Integer::class.java.isAssignableFrom(type.clazz) ->
                            Commands
                                .argument("value", IntegerArgumentType.integer())
                                .executes { ctx ->
                                    configurer.set(
                                        ctx,
                                        type as NoxesiumComponentType<Int>,
                                        IntegerArgumentType.getInteger(ctx, "value"),
                                    )
                                }

                        java.lang.Boolean::class.java.isAssignableFrom(type.clazz) ->
                            Commands
                                .argument("value", BoolArgumentType.bool())
                                .executes { ctx ->
                                    configurer.set(
                                        ctx,
                                        type as NoxesiumComponentType<Boolean>,
                                        BoolArgumentType.getBool(ctx, "value"),
                                    )
                                }

                        Color::class.java.isAssignableFrom(type.clazz) ->
                            Commands
                                .argument("value", ArgumentTypes.hexColor())
                                .executes { ctx ->
                                    configurer.set(
                                        ctx,
                                        type as NoxesiumComponentType<Color>,
                                        Color(ctx.getArgument("value", TextColor::class.java).value()),
                                    )
                                }

                        Vector3f::class.java.isAssignableFrom(type.clazz) ->
                            Commands
                                .argument("x", FloatArgumentType.floatArg())
                                .then(
                                    Commands
                                        .argument("y", FloatArgumentType.floatArg())
                                        .then(
                                            Commands
                                                .argument("z", FloatArgumentType.floatArg())
                                                .executes { ctx ->
                                                    configurer.set(
                                                        ctx,
                                                        type as NoxesiumComponentType<Vector3f>,
                                                        Vector3f(
                                                            FloatArgumentType.getFloat(ctx, "x"),
                                                            FloatArgumentType.getFloat(ctx, "y"),
                                                            FloatArgumentType.getFloat(ctx, "z"),
                                                        ),
                                                    )
                                                },
                                        ),
                                )

                        ItemStack::class.java.isAssignableFrom(type.clazz) ->
                            Commands
                                .argument("value", ArgumentTypes.itemStack())
                                .executes { ctx ->
                                    configurer.set(
                                        ctx,
                                        type as NoxesiumComponentType<ItemStack>,
                                        CraftItemStack.unwrap(ctx.getArgument("value", org.bukkit.inventory.ItemStack::class.java)),
                                    )
                                }

                        GraphicsMode::class.java.isAssignableFrom(type.clazz) -> {
                            forEnum(GraphicsMode::class.java)
                            continue
                        }

                        Unit::class.java.isAssignableFrom(type.clazz) -> {
                            // Units are special and have no more parameters!
                            branch.then(
                                Commands
                                    .literal(type.id.asString())
                                    .executes { ctx -> configurer.set(ctx, type as NoxesiumComponentType<Unit>, Unit.INSTANCE) },
                            )
                            validIds += type
                            continue
                        }

                        // We cannot provide methods to set anything else!
                        else -> continue
                    }

                validIds += type
                branch.then(Commands.literal(type.id.asString()).then(argumentBuilder))
            }
        },
    )
    then(
        Commands
            .literal("get")
            .apply {
                for (type in validIds) {
                    then(
                        Commands
                            .literal(type.id.asString())
                            .executes { ctx -> configurer.get(ctx, type) },
                    )
                }
            },
    )
    then(
        Commands
            .literal("reset")
            .apply {
                for (type in validIds) {
                    then(
                        Commands
                            .literal(type.id.asString())
                            .executes { ctx -> configurer.set(ctx, type, null) },
                    )
                }
            },
    )
    return this
}
