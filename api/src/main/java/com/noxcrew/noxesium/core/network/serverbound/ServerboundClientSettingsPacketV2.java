package com.noxcrew.noxesium.core.network.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.core.feature.ClientSettings;

/**
 * Sent to the server to inform it about various settings configured by the client,
 * mostly geared towards the size of their GUI and other visual-related settings.
 * <p>
 * Includes chat visibility, chat width, chat height and FOV values which were not present on the V1 packet.
 */
public record ServerboundClientSettingsPacketV2(ClientSettings settings) implements NoxesiumPacket {}
