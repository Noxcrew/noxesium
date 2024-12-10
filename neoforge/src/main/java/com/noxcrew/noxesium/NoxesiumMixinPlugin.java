package com.noxcrew.noxesium;

import net.neoforged.fml.loading.LoadingModList;

/**
 * Implements NoxesiumMixinPluginBase for NeoForge.
 */
public class NoxesiumMixinPlugin extends NoxesiumMixinPluginBase {

    @Override
    protected boolean isModLoaded(String modName) {
        return LoadingModList.get().getModFileById(modName) != null;
    }
}
