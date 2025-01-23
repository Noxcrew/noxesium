package com.noxcrew.noxesium.api.qib;

public enum QibOperation {
    ADD,
    SET,
    MUL,
    DIV,
    MIN,
    MAX
    ;

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