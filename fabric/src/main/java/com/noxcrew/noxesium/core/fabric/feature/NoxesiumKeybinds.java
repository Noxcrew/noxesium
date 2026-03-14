package com.noxcrew.noxesium.core.fabric.feature;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

/**
 * Provides all available keybinds.
 */
public class NoxesiumKeybinds {
    /**
     * Opens the Noxesium debug menu when combined with F3.
     */
    public final KeyMapping keyDebugNoxesium =
            register("key.debug.noxesium", InputConstants.Type.KEYSYM, InputConstants.KEY_W, KeyMapping.Category.DEBUG);

    /**
     * Registers a new key binding with the given handler.
     */
    private KeyMapping register(
            String translationKey, InputConstants.Type type, int code, KeyMapping.Category category) {
        var keyMapping = new KeyMapping(translationKey, type, code, category);
        KeyMappingHelper.registerKeyMapping(keyMapping);
        return keyMapping;
    }
}
