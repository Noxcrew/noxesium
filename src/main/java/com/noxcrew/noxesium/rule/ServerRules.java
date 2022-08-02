package com.noxcrew.noxesium.rule;

/**
 * A class that stores all known server rules.
 */
public class ServerRules {
    /**
     * If `true` disables the riptide spin attack on the trident from colliding with any entities,
     * useful for non-pvp mini-games where the trident is used as a movement tool.
     */
    public static ServerRule<Boolean> DISABLE_AUTO_SPIN_ATTACK = new BooleanServerRule(0, false);
}
