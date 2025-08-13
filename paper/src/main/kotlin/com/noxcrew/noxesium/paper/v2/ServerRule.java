package com.noxcrew.noxesium.paper.v2;

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
     * Writes the given value to a buffer.
     */
    public abstract void write(ValueT value, BufferT buffer);

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
     * Sets the value of this rule to the given value, performs
     * an unsafe cast to ValueT!
     */
    public void setUnsafe(Object value) {
        setValue((ValueT) value);
    }

    /**
     * Writes the given value to the buffer, performs an unsafe
     * cast to ValueT!
     */
    public void writeUnsafe(Object value, BufferT buffer) {
        write((ValueT) value, buffer);
    }
}
