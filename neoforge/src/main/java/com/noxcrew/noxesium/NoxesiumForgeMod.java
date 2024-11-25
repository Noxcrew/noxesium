package com.noxcrew.noxesium;

import com.noxcrew.noxesium.config.NoxesiumSettingsScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Defines the main mod for NeoForge.
 */
@Mod(value = "noxesium", dist = Dist.CLIENT)
public class NoxesiumForgeMod {

    public NoxesiumForgeMod(ModContainer container) {
        // Register the configuration menu accessible from the mods menu
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, screen) -> new NoxesiumSettingsScreen(screen));

        // Set up the NoxesiumMod
        NoxesiumMod.configure(new NoxesiumForgeHook(container));
    }
}
