package com.noxcrew.noxesium;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/**
 * Implements platform hooks on Fabric.
 */
public class NoxesiumFabricHook implements NoxesiumPlatformHook {

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modName) {
        return FabricLoader.getInstance().isModLoaded(modName);
    }

    @Override
    public String getNoxesiumVersion() {
        return "fabric-" + FabricLoader.getInstance().getModContainer(NoxesiumReferences.NAMESPACE).map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
    }
}
