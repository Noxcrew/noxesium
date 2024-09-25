package com.noxcrew.noxesium.feature.rule;

import com.noxcrew.noxesium.api.protocol.rule.ServerRule;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.Objects;

/**
 * A generic object whose value is filled in by a server.
 */
public abstract class ClientServerRule<T> extends ServerRule<T, RegistryFriendlyByteBuf> {

    private final int index;
    private T value;

    public ClientServerRule(int index) {
        this.index = index;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        var oldValue = this.value;
        this.value = value;

        if (!Objects.equals(oldValue, value)) {
            onValueChanged(oldValue, value);
        }
    }

    @Override
    public int getIndex() {
        return index;
    }

    /**
     * Called when the value changes from [oldValue] to [newValue].
     */
    protected void onValueChanged(T oldValue, T newValue) {
    }

    // On the client we implement write because Lunar re-serializes packets.
}
