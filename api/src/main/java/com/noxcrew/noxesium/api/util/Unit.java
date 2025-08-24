package com.noxcrew.noxesium.api.util;

/**
 * A simple no-op class.
 */
public class Unit {
    public static final Unit INSTANCE = new Unit();

    @Override
    public String toString() {
        return "Unit";
    }
}
