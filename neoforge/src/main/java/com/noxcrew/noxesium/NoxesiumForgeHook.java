package com.noxcrew.noxesium;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

/**
 * Implements platform hooks on Fabric.
 */
public record NoxesiumForgeHook(ModContainer modContainer) implements NoxesiumPlatformHook {

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isModLoaded(String modName) {
        return ModList.get().isLoaded(modName);
    }

    @Override
    public String getNoxesiumVersion() {
        return modContainer.getModInfo().getVersion().toString();
    }
}
