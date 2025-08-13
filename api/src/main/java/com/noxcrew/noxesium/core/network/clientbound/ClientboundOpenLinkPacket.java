package com.noxcrew.noxesium.core.network.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.Optional;
import net.kyori.adventure.text.Component;

/**
 * Sent by the server to open a link dialog on the client.
 */
public record ClientboundOpenLinkPacket(Optional<Component> text, String url) implements NoxesiumPacket {}
