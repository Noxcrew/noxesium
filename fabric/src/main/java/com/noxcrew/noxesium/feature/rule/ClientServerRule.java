package com.noxcrew.noxesium.feature.rule;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.api.protocol.rule.ServerRule;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

/**
 * A generic object whose value is filled in by a server.
 */
public abstract class ClientServerRule<T> extends ServerRule<T, FriendlyByteBuf> {

    private final int index;
    private T value;

    public ClientServerRule(int index) {
        this.index = index;

        // Add any created server rule to the global list
        NoxesiumMod.getInstance().getModule(ServerRuleModule.class).register(index, this);
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
    public void write(T value, FriendlyByteBuf buffer) {
        throw new UnsupportedOperationException("Cannot write a client-side server rule to a buffer");
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
}
