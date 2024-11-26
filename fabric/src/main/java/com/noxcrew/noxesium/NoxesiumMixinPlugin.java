package com.noxcrew.noxesium;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Implements NoxesiumMixinPluginBase for Fabric.
 */
public class NoxesiumMixinPlugin extends NoxesiumMixinPluginBase {

    @Override
    protected boolean isModLoaded(String modName) {
        return FabricLoader.getInstance().isModLoaded(modName);
    }
}
