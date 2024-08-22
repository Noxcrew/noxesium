package com.noxcrew.noxesium.config.sodium;

import com.noxcrew.noxesium.config.NoxesiumConfig;
import com.noxcrew.noxesium.NoxesiumMod;
import net.caffeinemc.mods.sodium.client.gui.options.storage.OptionStorage;

/**
 * Implements Sodium's option storage for Noxesium's options.
 */
public class NoxesiumOptionStorage implements OptionStorage<NoxesiumConfig> {

    @Override
    public NoxesiumConfig getData() {
        return NoxesiumMod.getInstance().getConfig();
    }

    @Override
    public void save() {
        NoxesiumMod.getInstance().getConfig().save();
    }
}
