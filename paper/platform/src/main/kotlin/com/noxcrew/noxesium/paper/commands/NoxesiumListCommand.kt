package com.noxcrew.noxesium.paper.commands

import com.noxcrew.noxesium.api.network.EntrypointProtocol
import com.noxcrew.noxesium.core.nms.network.NoxesiumPlayerManager
import com.noxcrew.noxesium.core.util.SkullStringFormatter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Defines a command that shows the version of Noxesium being used by all
 * online players.
 */
public class NoxesiumListCommand : CommandExecutor {
    private data class PlayerInfo(
        val player: Player,
        val baseProtocol: EntrypointProtocol?,
        val otherEntrypoints: List<EntrypointProtocol>,
    )

    /** Executes the command for the given [sender]. */
    public fun execute(sender: CommandSender) {
        // Determine all players to show and their data
        val players = mutableListOf<PlayerInfo>()
        for (player in Bukkit.getOnlinePlayers()) {
            val data = NoxesiumPlayerManager.getInstance().getPlayer(player.uniqueId) ?: continue
            val baseProtocol = data.supportedEntrypoints.firstOrNull { it.id == "noxesium-common" } ?: continue
            val otherProtocols = data.supportedEntrypoints.filter { it.id != "noxesium-common" }
            players += PlayerInfo(player, baseProtocol, otherProtocols)
        }

        // Sort by the protocol version of the base protocol
        val grouped = players.groupBy { it.baseProtocol }

        // Show the information for each player
        for ((protocol, playerInfos) in grouped.entries.sortedBy { it.key?.protocolVersion ?: -1 }) {
            sender.sendMessage(
                Component.join(
                    JoinConfiguration.noSeparators(),
                    buildList {
                        add(text("[", NamedTextColor.DARK_GRAY))
                        add(
                            if (protocol == null) {
                                text("None", NamedTextColor.YELLOW)
                            } else {
                                text("${protocol.rawVersion} (${protocol.protocolVersion})", NamedTextColor.GOLD)
                            },
                        )
                        add(text("] (", NamedTextColor.DARK_GRAY))
                        add(text(playerInfos.size.toString(), NamedTextColor.GRAY))
                        add(text("): ", NamedTextColor.DARK_GRAY))
                        add(text("[", NamedTextColor.AQUA))
                        for (index in playerInfos.indices) {
                            val info = playerInfos[index]
                            val last = index == playerInfos.size - 1

                            add(
                                Component.translatable(
                                    SkullStringFormatter.write(
                                        SkullStringFormatter.SkullInfo(
                                            false,
                                            info.player.uniqueId.toString(),
                                            false,
                                            0,
                                            0,
                                            1f,
                                        ),
                                    ),
                                    // Show as empty for non-Noxesium clients
                                    "",
                                ),
                            )
                            add(info.player.displayName().color(NamedTextColor.AQUA))
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

    override fun onCommand(sender: CommandSender, command: Command, text: String, args: Array<out String>): Boolean {
        execute(sender)
        return true
    }
}
