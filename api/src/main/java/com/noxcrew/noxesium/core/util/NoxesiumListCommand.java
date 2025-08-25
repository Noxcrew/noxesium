package com.noxcrew.noxesium.core.util;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Defines a command that shows the version of Noxesium being used by all
 * online players.
 */
public class NoxesiumListCommand {
    /**
     * Stores relevant information on a player.
     */
    public static record PlayerInfo(
            UUID uuid,
            Component displayName,
            EntrypointProtocol baseProtocol,
            List<EntrypointProtocol> otherProtocols) {}

    /**
     * Formats a line showing multiple players.
     */
    public static Component formatLine(Component version, List<PlayerInfo> players) {
        var components = new ArrayList<Component>();
        components.add(Component.text("[", NamedTextColor.DARK_GRAY));
        components.add(version);
        components.add(Component.text("] (", NamedTextColor.DARK_GRAY));
        components.add(Component.text(Integer.toString(players.size()), NamedTextColor.GRAY));
        components.add(Component.text("):", NamedTextColor.DARK_GRAY));
        components.add(Component.text("[", NamedTextColor.AQUA));
        for (var index = 0; index < players.size(); index++) {
            var player = players.get(index);
            HoverEvent<Component> hoverEvent = null;
            if (!player.otherProtocols.isEmpty()) {
                // Show other entrypoints in the hover tooltip
                var text = new ArrayList<Component>();
                text.add(Component.text("Other entrypoints:", NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD));
                for (var other : player.otherProtocols) {
                    text.add(Component.text(
                            " - " + other.id() + ": " + other.rawVersion() + " (" + other.protocolVersion() + ")",
                            NamedTextColor.GRAY));
                }
                hoverEvent = HoverEvent.showText(Component.join(JoinConfiguration.newlines(), text));
            }

            components.add(Component.translatable(
                            SkullStringFormatter.write(new SkullStringFormatter.SkullInfo(
                                    false, player.uuid().toString(), false, 0, 0, 1f)),
                            // Show as empty for non-Noxesium clients
                            "")
                    .hoverEvent(hoverEvent));
            components.add(player.displayName().color(NamedTextColor.AQUA).hoverEvent(hoverEvent));
            if (index != players.size() - 1) {
                components.add(Component.text(", ", NamedTextColor.AQUA));
            }
        }
        components.add(Component.text("]", NamedTextColor.AQUA));
        return Component.join(JoinConfiguration.noSeparators(), components);
    }

    /**
     * Sends the given receiver a list of Noxesium users and their protocol versions in chat.
     * Returns the ids of all users that were listed.
     */
    public static List<UUID> sendUserList(Audience receiver) {
        // Determine all players to show and their data
        var players = new HashMap<EntrypointProtocol, List<PlayerInfo>>();
        var listed = new ArrayList<UUID>();
        for (var player : NoxesiumPlayerManager.getInstance().getAllPlayers()) {
            // Determine which protocols each player has
            EntrypointProtocol baseProtocol = null;
            var otherProtocols = new ArrayList<EntrypointProtocol>();
            for (var entrypoint : player.getSupportedEntrypoints()) {
                if (entrypoint.id().equals(NoxesiumReferences.COMMON_ENTRYPOINT)) {
                    baseProtocol = entrypoint;
                } else {
                    otherProtocols.add(entrypoint);
                }
            }
            if (baseProtocol == null) continue;
            listed.add(player.getUniqueId());
            players.computeIfAbsent(baseProtocol, (it) -> new ArrayList<>())
                    .add(new PlayerInfo(player.getUniqueId(), player.getDisplayName(), baseProtocol, otherProtocols));
        }

        // Sort by the protocol version of the base protocol
        var orderedKeys = new ArrayList<>(players.keySet());
        orderedKeys.sort(Comparator.comparing(EntrypointProtocol::protocolVersion));

        // Show the information for each player
        for (var protocol : orderedKeys) {
            var playerInfos = players.get(protocol);
            if (playerInfos == null) continue;
            receiver.sendMessage(formatLine(
                    Component.text(
                            protocol.rawVersion() + " (" + protocol.protocolVersion() + ")", NamedTextColor.GOLD),
                    playerInfos));
        }
        return listed;
    }
}
