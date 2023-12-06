package com.noxcrew.noxesium.compat;

import com.noxcrew.noxesium.util.CompatibilityReferences;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Integrates with mod menu to add a configuration screen.
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> CompatibilityReferences.isUsingClothConfig() ? ClothConfigIntegration.getMenu(screen) : null;
    }
}
