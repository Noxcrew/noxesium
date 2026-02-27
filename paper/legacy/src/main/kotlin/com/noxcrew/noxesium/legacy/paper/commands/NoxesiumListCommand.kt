package com.noxcrew.noxesium.legacy.paper.commands

import com.noxcrew.noxesium.legacy.paper.api.NoxesiumManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * Defines a command that shows the version of Noxesium being used by all
 * online players.
 */
public class NoxesiumListCommand(
    private val noxesiumManager: NoxesiumManager,
) {
    /** Executes the command for the given [sender]. */
    public fun execute(sender: CommandSender) {
        val input =
            Bukkit.getOnlinePlayers().groupBy {
                val protocol = noxesiumManager.getProtocolVersion(it)
                val exact =
                    noxesiumManager.getExactVersion(it) ?: when (protocol) {
                        // These are only used for old clients but we add all versions here
                        // to keep a proper record of when protocol versions were raised.
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
                        13 -> "v2.6.0"
                        14 -> "v2.6.1"
                        15 -> "v2.6.2"
                        16 -> "v2.7.3"
                        17 -> "v2.7.4"
                        18 -> "v2.7.5"
                        else -> "None"
                    }
                val display = if (protocol != null) text("$exact ($protocol)", NamedTextColor.GOLD) else text("None", NamedTextColor.YELLOW)
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
                                    "%nox_uuid%${player.uniqueId},false,0,0,1",
                                    // Show as empty for non-Noxesium clients
                                    "",
                                ),
                            )
                            add(player.displayName().color(NamedTextColor.AQUA))
                            if (!last) {
                                add(text(", ", NamedTextColor.AQUA))
                            }
                        }
                        add(text("]", NamedTextColor.AQUA))
                    },
                ),
            )
        }
    }
}
