package com.noxcrew.noxesium.api.qib;

/**
 * Represents a mathematical operation/expression that can be applied to a value.
 */
public enum QibOperation {
    ADD,
    SET,
    MUL,
    DIV,
    MIN,
    MAX
    ;

    /**
     * Applies the operation to the given value with the given modifier resulting in the new value.
     * <p/>
     * The operation is applied as follows:
     * <ul>
     *     <li>{@link #ADD}: value + modifier</li>
     *     <li>{@link #SET}: modifier</li>
     *     <li>{@link #MUL}: value * modifier</li>
     *     <li>{@link #DIV}: value / modifier</li>
     *     <li>{@link #MIN}: min(value, modifier)</li>
     *     <li>{@link #MAX}: max(value, modifier)</li>
     * </ul>
     * @param value the value to apply the operation to
     * @param modifier the modifier to apply to the value
     * @return the result of the operation
     */
    public double apply(double value, double modifier) {
        return switch (this) {
            case ADD -> value + modifier;
            case SET -> modifier;
            case MUL -> value * modifier;
            case DIV -> value / modifier;
            case MIN -> Math.min(value, modifier);
            case MAX -> Math.max(value, modifier);
        };
    }
}