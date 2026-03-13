package com.noxcrew.noxesium.core.feature;

/**
 * Different supporting types of easing functions.
 */
public enum EasingType {
    LINEAR {
        @Override
        protected double ease(double value) {
            return value;
        }
    },
    EASE_IN {
        @Override
        protected double ease(double value) {
            return value * value;
        }
    },
    EASE_OUT {
        @Override
        protected double ease(double value) {
            return 1.0 - (1.0 - value) * (1.0 - value);
        }
    },
    EASE_IN_OUT {
        @Override
        protected double ease(double value) {
            return value < 0.5 ? 2.0 * value * value : 1.0 - Math.pow(-2.0 * value + 2.0, 2.0) / 2.0;
        }
    },
    EASE_IN_CUBIC {
        @Override
        protected double ease(double value) {
            return value * value * value;
        }
    },
    EASE_OUT_CUBIC {
        @Override
        protected double ease(double value) {
            return 1.0 - Math.pow(1.0 - value, 3.0);
        }
    },
    EASE_IN_OUT_CUBIC {
        @Override
        protected double ease(double value) {
            return value < 0.5 ? 4.0 * value * value * value : 1.0 - Math.pow(-2.0 * value + 2.0, 3.0) / 2.0;
        }
    },
    EASE_IN_SINE {
        @Override
        protected double ease(double value) {
            return 1.0 - Math.cos((value * Math.PI) / 2.0);
        }
    },
    EASE_OUT_SINE {
        @Override
        protected double ease(double value) {
            return Math.sin((value * Math.PI) / 2.0);
        }
    },
    EASE_IN_OUT_SINE {
        @Override
        protected double ease(double value) {
            return -(Math.cos(Math.PI * value) - 1.0) / 2.0;
        }
    };

    /**
     * Applies this easing type to the given parameter.
     */
    protected abstract double ease(double value);

    /**
     * Applies this easing type to the given parameter.
     */
    public double apply(double value) {
        return ease(Math.clamp(value, 0.0, 1.0));
    }
}
