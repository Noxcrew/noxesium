package com.noxcrew.noxesium.rule;

import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A generic object whose value is filled in by a server.
 */
public abstract class ServerRule<T> {

    private static final Map<Integer, ServerRule<?>> rules = new HashMap<>();
    private T value = getDefault();

    public ServerRule(int index) {
        // Add any created server rule to the global list
        rules.put(index, this);
    }

    /**
     * Returns the default value of this rule.
     */
    protected abstract T getDefault();

    /**
     * Reads this value from a buffer.
     */
    public abstract T read(FriendlyByteBuf buffer);

    /**
     * Returns the value of this rule.
     */
    public T get() {
        return value;
    }

    /**
     * Resets the value of this rule.
     */
    private void reset() {
        var oldValue = value;
        value = getDefault();

        if (!Objects.equals(oldValue, value)) {
            onValueChanged(oldValue, value);
        }
    }

    /**
     * Reads the value of this rule from the given [buffer].
     */
    private void set(FriendlyByteBuf buffer) {
        var oldValue = value;
        value = read(buffer);

        if (!Objects.equals(oldValue, value)) {
            onValueChanged(oldValue, value);
        }
    }

    /**
     * Called when the value changes from [oldValue] to [newValue].
     */
    protected void onValueChanged(T oldValue, T newValue) {
    }

    /**
     * Reads the data for all rules from the server.
     */
    public static void readAll(FriendlyByteBuf buffer) {
        // First we get a list of ints to clear the data for
        var clear = buffer.readVarIntArray();
        for (var index : clear) {
            var rule = rules.get(index);
            if (rule == null) continue;
            rule.reset();
        }

        // Second we let each value read the data from the buffer
        var amount = buffer.readVarInt();
        for (int i = 0; i < amount; i++) {
            var index = buffer.readVarInt();
            var rule = rules.get(index);
            if (rule == null) continue;
            rule.set(buffer);
        }
    }

    /**
     * Clears the stored data for all server rules.
     */
    public static void clearAll() {
        for (var rule : rules.values()) {
            rule.reset();
        }
    }
}
