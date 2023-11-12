package com.noxcrew.noxesium.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Integrates with mod menu to add a configuration screen.
 */
public class ModMenuIntegration implements ModMenuApi {

    private final boolean isUsingClothConfig = FabricLoader.getInstance().isModLoaded("cloth-config");

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> isUsingClothConfig ? ClothConfigIntegration.getMenu(screen) : null;
    }
}
