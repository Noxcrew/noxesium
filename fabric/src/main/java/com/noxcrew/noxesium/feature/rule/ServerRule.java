package com.noxcrew.noxesium.feature.rule;

import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

/**
 * A generic object whose value is filled in by a server.
 */
public abstract class ServerRule<T> {

    private T value = getDefault();

    public ServerRule(int index) {
        // Add any created server rule to the global list
        ServerRuleModule.getInstance().register(index, this);
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
}
