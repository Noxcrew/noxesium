package com.noxcrew.noxesium.core.mcc;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.List;

/**
 * Sent by MCC Island whenever you switch servers. All values are dynamic and may change over time.
 *
 * @param server The type of server connected to.
 * @param types  A list of types of the current sub-server or area.
 */
public record ClientboundMccServerPacket(String server, List<String> types) implements NoxesiumPacket {}
