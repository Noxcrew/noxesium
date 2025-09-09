package com.noxcrew.noxesium.api.player;

import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.core.client.setting.ClientSettings;
import java.util.ArrayList;
import java.util.List;

/**
 * A serialized representation of a Noxesium player
 * that can be synchronized across multiple servers
 * behind a proxy using e.g. Redis.
 */
public record SerializedNoxesiumServerPlayer(List<EntrypointProtocol> supportedEntrypoints, ClientSettings settings) {

    public SerializedNoxesiumServerPlayer() {
        this(new ArrayList<>(), null);
    }
}
