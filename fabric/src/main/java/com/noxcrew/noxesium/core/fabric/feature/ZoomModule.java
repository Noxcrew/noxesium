package com.noxcrew.noxesium.core.fabric.feature;

import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.core.feature.EasingType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.Mth;

public class ZoomModule extends NoxesiumFeature {

    private float currentZoom = 1.0f;
    private float previousZoom = 1.0f;
    private float startZoom = 1.0f;
    private float targetZoom = 1.0f;

    private int transitionTicksTotal = 0;
    private int transitionTicksRemaining = 0;

    private EasingType easingType = EasingType.LINEAR;
    private boolean lockClientFov = false;
    private boolean keepHandStationary = true;

    public ZoomModule() {
        ClientTickEvents.END_CLIENT_TICK.register((ignored) -> {
            if (!isRegistered()) return;
            tick();
        });
    }

    public float getZoom(float tickDelta) {
        return Mth.lerp(tickDelta, previousZoom, currentZoom);
    }

    public boolean isLockClientFov() {
        return lockClientFov;
    }

    public void applyZoom(
            float zoom, int transitionTicks, EasingType easingType, boolean lockClientFov, boolean keepHandStationary) {
        this.lockClientFov = lockClientFov;
        this.keepHandStationary = keepHandStationary;

        if (transitionTicks <= 0) {
            this.currentZoom = zoom;
            this.startZoom = zoom;
            this.targetZoom = zoom;
            this.transitionTicksTotal = 0;
            this.transitionTicksRemaining = 0;
            this.easingType = easingType;
            return;
        }

        this.startZoom = this.currentZoom;
        this.targetZoom = zoom;
        this.transitionTicksTotal = transitionTicks;
        this.transitionTicksRemaining = transitionTicks;
        this.easingType = easingType;
    }

    public void reset() {
        this.currentZoom = 1.0f;
        this.startZoom = 1.0f;
        this.targetZoom = 1.0f;
        this.transitionTicksTotal = 0;
        this.transitionTicksRemaining = 0;
        this.lockClientFov = false;
        this.keepHandStationary = true;
        this.easingType = EasingType.LINEAR;
    }

    private void tick() {
        previousZoom = currentZoom;

        if (transitionTicksRemaining <= 0 || transitionTicksTotal <= 0) {
            currentZoom = targetZoom;
            return;
        }

        transitionTicksRemaining--;

        var total = (float) transitionTicksTotal;
        var elapsed = (float) (transitionTicksTotal - transitionTicksRemaining);
        var t = total <= 0.0f ? 1.0f : (elapsed / total);
        if (t < 0.0f) t = 0.0f;
        if (t > 1.0f) t = 1.0f;

        var eased = (float) ease(t, easingType);
        currentZoom = startZoom + (targetZoom - startZoom) * eased;

        if (transitionTicksRemaining <= 0) {
            currentZoom = targetZoom;
        }
    }

    private static double ease(double t, EasingType type) {
        return switch (type) {
            case LINEAR -> t;
            case EASE_IN -> t * t;
            case EASE_OUT -> 1.0 - (1.0 - t) * (1.0 - t);
            case EASE_IN_OUT -> t < 0.5 ? 2.0 * t * t : 1.0 - Math.pow(-2.0 * t + 2.0, 2.0) / 2.0;
            case EASE_IN_CUBIC -> t * t * t;
            case EASE_OUT_CUBIC -> 1.0 - Math.pow(1.0 - t, 3.0);
            case EASE_IN_OUT_CUBIC -> t < 0.5 ? 4.0 * t * t * t : 1.0 - Math.pow(-2.0 * t + 2.0, 3.0) / 2.0;
            case EASE_IN_SINE -> 1.0 - Math.cos((t * Math.PI) / 2.0);
            case EASE_OUT_SINE -> Math.sin((t * Math.PI) / 2.0);
            case EASE_IN_OUT_SINE -> -(Math.cos(Math.PI * t) - 1.0) / 2.0;
        };
    }

    public boolean isKeepHandStationary() {
        return keepHandStationary;
    }
}
