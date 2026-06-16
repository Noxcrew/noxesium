package com.noxcrew.noxesium.api.util;

import org.joml.Vector3f;

/** The different axis of a vector. */
public enum VectorAxis {
    X {
        @Override
        public float select(Vector3f vector) {
            return vector.x;
        }
    },
    Y {
        @Override
        public float select(Vector3f vector) {
            return vector.y;
        }
    },
    Z {
        @Override
        public float select(Vector3f vector) {
            return vector.z;
        }
    };

    /** Selects the axis from the given vector. */
    public abstract float select(Vector3f vector);
}
