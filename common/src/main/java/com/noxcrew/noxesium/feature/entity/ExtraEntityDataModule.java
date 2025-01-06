package com.noxcrew.noxesium.feature.entity;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.NoxesiumModule;
import com.noxcrew.noxesium.feature.rule.ClientServerRule;
import com.noxcrew.noxesium.feature.rule.RuleIndexProvider;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores information about the currently known extra entity data keys.
 * Uses the server rule system to lend its (de)serialization and indexing logic.
 */
public class ExtraEntityDataModule implements NoxesiumModule, RuleIndexProvider {

    private final Map<Integer, ClientServerRule<?>> rules = new HashMap<>();

    /**
     * Registers a new extra entity data key with the given index and data.
     *
     * @param index The index of this data, must be unique.
     * @param rule  The object with the data for this data.
     */
    public void register(int index, ClientServerRule<?> rule) {
        Preconditions.checkArgument(
                !rules.containsKey(index), "Index " + index + " was used by multiple entity data objects");
        rules.put(index, rule);
    }

    @Override
    public ClientServerRule<?> getIndex(int index) {
        return rules.get(index);
    }
}
