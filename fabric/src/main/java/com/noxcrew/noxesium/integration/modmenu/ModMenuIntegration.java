package com.noxcrew.noxesium.integration.modmenu;

import com.noxcrew.noxesium.config.NoxesiumSettingsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Adds a custom settings menu when clicking on Noxesium in Mod Menu.
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return NoxesiumSettingsScreen::new;
    }
}
