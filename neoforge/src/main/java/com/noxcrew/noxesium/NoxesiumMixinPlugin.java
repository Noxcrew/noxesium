package com.noxcrew.noxesium;

import net.neoforged.fml.ModList;

/**
 * Implements NoxesiumMixinPluginBase for NeoForge.
 */
public class NoxesiumMixinPlugin extends NoxesiumMixinPluginBase {

    @Override
    protected boolean isModLoaded(String modName) {
        return ModList.get().isLoaded(modName);
    }
}
