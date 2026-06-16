package com.noxcrew.noxesium.core.fabric.feature;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.resources.Identifier;

/**
 * Helps load game component information.
 */
public class GameComponentHelper {
    /**
     * Returns whether the debug entry with the given key is allowed, falling back
     * to the vanilla fallback value.
     */
    public static boolean isDebugEntryAllowed(Identifier key, boolean fallback) {
        var string = key.toString();
        var denied = GameComponents.getInstance().noxesium$getComponent(CommonGameComponentTypes.DEBUG_ENTRIES_DENIED);
        if (denied != null && denied.contains(string)) return false;

        var allowed =
                GameComponents.getInstance().noxesium$getComponent(CommonGameComponentTypes.DEBUG_ENTRIES_ALLOWED);
        if (allowed != null && allowed.contains(string)) return true;

        var override =
                GameComponents.getInstance().noxesium$getComponent(CommonGameComponentTypes.DEBUG_ENTRIES_OVERRIDE);
        if (override != null) {
            return override.contains(string);
        }

        return fallback;
    }
}
