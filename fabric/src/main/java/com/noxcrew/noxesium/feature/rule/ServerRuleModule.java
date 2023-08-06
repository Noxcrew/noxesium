package com.noxcrew.noxesium.feature.rule;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.NoxesiumModule;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores information about the currently known server rules and their data.
 */
public class ServerRuleModule implements NoxesiumModule {

    private static ServerRuleModule instance;

    /**
     * Returns the singleton instance of the server rule module.
     */
    public static ServerRuleModule getInstance() {
        if (instance == null) {
            instance = new ServerRuleModule();
        }
        return instance;
    }

    private final Map<Integer, ClientServerRule<?>> rules = new HashMap<>();

    @Override
    public void onQuitServer() {
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

    /**
     * Returns the rule saved under the given index.
     */
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
