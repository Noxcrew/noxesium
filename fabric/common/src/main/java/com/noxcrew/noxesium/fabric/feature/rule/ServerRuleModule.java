package com.noxcrew.noxesium.fabric.feature.rule;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.fabric.feature.NoxesiumFeature;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Stores information about the currently known server rules and their data.
 */
public class ServerRuleModule implements NoxesiumFeature, RuleIndexProvider {

    /**
     * If enabled settings are not overridden. This should be true while rendering the settings menu.
     */
    public static boolean noxesium$disableSettingOverrides = false;

    /**
     * Whether Iris is being used. If true we don't allow the graphics setting to be changed to Fabulous! as
     * to not break Iris.
     */
    public static boolean noxesium$isUsingIris = false;

    private final Map<Integer, ClientServerRule<?>> rules = new HashMap<>();

    public ServerRuleModule() {
        // Store whether we are using Iris or not
        noxesium$isUsingIris = FabricLoader.getInstance().isModLoaded("iris");
    }

    @Override
    public void onUnregister() {
        // Clear all stored server rules
        clearAll();
    }

    /**
     * Registers a new server rule with the given index and data.
     *
     * @param index The index of this rule, must be unique.
     * @param rule  The object with the data for this rule.
     */
    public void register(int index, ClientServerRule<?> rule) {
        Preconditions.checkArgument(!rules.containsKey(index), "Index " + index + " was used by multiple server rules");
        rules.put(index, rule);
    }

    @Override
    public ClientServerRule<?> getIndex(int index) {
        return rules.get(index);
    }

    /**
     * Clears the stored data for all server rules.
     */
    public void clearAll() {
        for (var rule : rules.values()) {
            rule.reset();
        }
    }
}
