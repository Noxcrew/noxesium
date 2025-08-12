package com.noxcrew.noxesium.fabric.feature.rule;

/**
 * The basis for some Noxesium module that provides rules.
 */
public interface RuleIndexProvider {

    /**
     * Returns the rule saved under the given index.
     */
    public ClientServerRule<?> getIndex(int index);
}
