package com.noxcrew.noxesium.feature.rule;

import com.google.common.base.Preconditions;
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
        register(index, this);
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
    public void reset() {
        var oldValue = value;
        value = getDefault();

        if (!Objects.equals(oldValue, value)) {
            onValueChanged(oldValue, value);
        }
    }

    /**
     * Reads the value of this rule from the given [buffer].
     */
    public void set(FriendlyByteBuf buffer) {
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
     * Registers a new server rule with the given index and data.
     *
     * @param index The index of this rule, must be unique.
     * @param rule  The object with the data for this rule.
     */
    public static void register(int index, ServerRule<?> rule) {
        Preconditions.checkArgument(!rules.containsKey(index), "Index " + index + " was used by multiple server rules");
        rules.put(index, rule);
    }

    /**
     * Returns the rule saved under the given index.
     */
    public static ServerRule<?> getIndex(int index) {
        return rules.get(index);
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
