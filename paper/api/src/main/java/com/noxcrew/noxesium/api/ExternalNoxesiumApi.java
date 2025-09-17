package com.noxcrew.noxesium.api;

import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Provides the external Noxesium API which forwards any requests to the Noxesium plugin if installed.
 */
public class ExternalNoxesiumApi {
    private static ExternalNoxesiumApi instance;

    /**
     * Returns the external Noxesium API instance.
     */
    public static ExternalNoxesiumApi getInstance() {
        if (instance == null) {
            try {
                var clazz = Class.forName("com.noxcrew.noxesium.paper.ExternalApi");
                return (ExternalNoxesiumApi) clazz.newInstance();
            } catch (Exception x) {
                // If we fail to load the class, fall back to no-op!
            }
            instance = new ExternalNoxesiumApi();
        }
        return instance;
    }

    /**
     * Returns whether the given player is using Noxesium.
     */
    public boolean isUsingNoxesium(UUID playerId) {
        return false;
    }

    /**
     * Returns whether the given player is using Noxesium.
     */
    public boolean isUsingNoxesium(Player player) {
        return isUsingNoxesium(player.getUniqueId());
    }

    /**
     * Returns all mods installed by the given player, if applicable.
     */
    public Map<String, String> getInstalledMods(UUID playerId) {
        return Map.of();
    }

    /**
     * Returns all mods installed by the given player, if applicable.
     */
    public Map<String, String> getInstalledMods(Player player) {
        return getInstalledMods(player.getUniqueId());
    }
}
