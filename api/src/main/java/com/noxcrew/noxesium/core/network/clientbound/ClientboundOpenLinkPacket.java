package com.noxcrew.noxesium.core.network.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import net.kyori.adventure.text.Component;

import java.util.Optional;

/**
 * Sent by the server to open a link dialog on the client.
 */
public record ClientboundOpenLinkPacket(Optional<Component> text, String url) implements NoxesiumPacket {}
