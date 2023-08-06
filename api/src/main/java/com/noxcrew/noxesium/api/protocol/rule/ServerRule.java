package com.noxcrew.noxesium.api.protocol.rule;

/**
 * A generic object whose value is filled in by a server.
 */
public abstract class ServerRule<ValueT, BufferT> {

    /**
     * Returns the index of this rule.
     */
    public abstract int getIndex();

    /**
     * Returns the default value of this rule.
     */
    protected abstract ValueT getDefault();

    /**
     * Reads this value from a buffer.
     */
    public abstract ValueT read(BufferT buffer);

    /**
     * Writes this value to a buffer.
     */
    public abstract void write(BufferT buffer);

    /**
     * Returns the value of this rule.
     */
    public abstract ValueT getValue();

    /**
     * Sets the value of this rule to the given new value.
     */
    public abstract void setValue(ValueT newValue);

    /**
     * Resets the value of this rule.
     */
    public void reset() {
        setValue(getDefault());
    }

    /**
     * Reads the value of this rule from the given [buffer].
     */
    public void setValueFromBuffer(BufferT buffer) {
        setValue(read(buffer));
    }
}
