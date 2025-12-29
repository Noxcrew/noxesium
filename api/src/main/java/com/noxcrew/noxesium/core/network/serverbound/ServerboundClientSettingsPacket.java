package com.noxcrew.noxesium.core.network.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.core.feature.ClientSettings;

/**
 * Sent to the server to inform it about various settings configured by the client,
 * mostly geared towards the size of their GUI and other visual-related settings.
 */
public record ServerboundClientSettingsPacket(ClientSettings settings) implements NoxesiumPacket {}
