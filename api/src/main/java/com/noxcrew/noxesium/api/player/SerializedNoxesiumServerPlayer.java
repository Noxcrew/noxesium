package com.noxcrew.noxesium.api.player;

import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.core.client.setting.ClientSettings;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A serialized representation of a Noxesium player
 * that can be synchronized across multiple servers
 * behind a proxy using e.g. Redis.
 */
public record SerializedNoxesiumServerPlayer(
        List<EntrypointProtocol> supportedEntrypoints,
        ClientSettings settings,
        Set<String> enabledLazyPackets,
        Map<String, String> mods,
        Map<String, Set<Integer>> knownIds,
        Map<String, Integer> knownHashes,
        Set<String> registeredPluginChannels) {}
