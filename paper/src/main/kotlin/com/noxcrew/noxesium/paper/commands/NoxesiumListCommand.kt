package com.noxcrew.noxesium.paper.commands

import com.noxcrew.noxesium.api.protocol.skull.SkullStringFormatter
import com.noxcrew.noxesium.paper.api.NoxesiumManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission

/**
 * Adds a command that shows the version of Noxesium being used by all
 * online players.
 */
public class NoxesiumListCommand(
    private val supplier: () -> NoxesiumManager,
) {

    @Command("noxlist|noxesiumlist")
    @Permission("mcc.admin.noxesium")
    public fun listNoxesiumUsers(sender: CommandSender) {
        val noxesiumManager = supplier()
        val input = Bukkit.getOnlinePlayers().groupBy {
            val protocol = noxesiumManager.getProtocolVersion(it)
            val exact = noxesiumManager.getExactVersion(it) ?: when (protocol) {
                0 -> "v0.1.0"
                1 -> "v0.1.0"
                2 -> "v0.1.6"
                3 -> "v1.0.0"
                4 -> "v1.1.1"
                5 -> "v1.2.1"
                6 -> "v2.0.0"
                7 -> "v2.1.0"
                8 -> "v2.1.2"
                9 -> "v2.2.0"
                10 -> "v2.3.0"
                11 -> "v2.3.2"
                12 -> "v2.4.0"
                else -> "None"
            }
            val display = if (protocol != null) text("$exact (${protocol})", NamedTextColor.GOLD) else text("None", NamedTextColor.YELLOW)
            (protocol ?: 0) to display
        }
        for (pair in input.keys.sortedBy { it.first }) {
            val (_, version) = pair
            val users = input[pair]!!

            sender.sendMessage(
                Component.join(
                    JoinConfiguration.noSeparators(),
                    buildList {
                        add(text("[", NamedTextColor.DARK_GRAY))
                        add(version)
                        add(text("] (", NamedTextColor.DARK_GRAY))
                        add(text(users.size.toString(), NamedTextColor.GRAY))
                        add(text("): ", NamedTextColor.DARK_GRAY))
                        add(text("[", NamedTextColor.AQUA))
                        for (index in users.indices) {
                            val player = users[index]
                            val last = index == users.size - 1

                            add(
                                Component.translatable(
                                    SkullStringFormatter.write(
                                        SkullStringFormatter.SkullInfo(
                                            false,
                                            player.uniqueId.toString(),
                                            false,
                                            0,
                                            0,
                                            1f,
                                        ),
                                    ),
                                    // Show as empty for non-Noxesium clients
                                    "",
                                )
                            )
                            add(player.displayName().color(NamedTextColor.AQUA))
                            if (!last) {
                                add(text(", ", NamedTextColor.AQUA))
                            }
                        }
                        add(text("]", NamedTextColor.AQUA))
                    }
                ),
            )
        }
    }
}