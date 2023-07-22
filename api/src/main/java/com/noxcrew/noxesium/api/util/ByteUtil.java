package com.noxcrew.noxesium.api.util;

/**
 * Provides utilities for storing additional details in a single byte value.
 */
public class ByteUtil {

    /**
     * Returns whether the flag at index in command is enabled.
     *
     * @param command A byte that contains multiple boolean values.
     * @param index   The index in the byte to read out.
     */
    public static boolean hasFlag(byte command, int index) {
        return (command & (1 << index)) != 0;
    }

    /**
     * Returns an edited version of the command to have the value at
     * the given index set to value.
     *
     * @param command A byte that contains multiple boolean values.
     * @param index   The index in the byte to write to.
     * @param value   The new value to store in the byte.
     */
    public static byte setFlag(byte command, int index, boolean value) {
        if (value) {
            return (byte) (command | (1 << index));
        } else {
            return (byte) (command & ~(1 << index));
        }
    }
}
