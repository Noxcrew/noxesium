package com.noxcrew.noxesium.core.fabric.feature;

import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.core.feature.EasingType;
import java.util.Optional;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/**
 * Adds support for the server modifying the current zoom level of the client.
 */
public class ZoomModule extends NoxesiumFeature {

    private double previousFov = -1.0;
    private double currentFov = -1.0;
    private double startFov = -1.0;
    private double targetFov = -1.0;

    private float currentZoom = 1.0f;
    private float previousZoom = 1.0f;
    private float startZoom = 1.0f;
    private float targetZoom = 1.0f;

    private int transitionTicksTotal = 0;
    private int transitionTicksRemaining = 0;

    private EasingType easingType = EasingType.LINEAR;
    private boolean lockClientFov = false;
    private boolean keepHandStationary = true;
    private boolean resetting = false;
    private boolean intendEditFov = false;

    public ZoomModule() {
        ClientTickEvents.END_CLIENT_TICK.register((ignored) -> {
            if (!isRegistered()) return;
            tick();
        });
    }

    /**
     * Returns the current zoom level given the tick delta.
     */
    public float getZoom(float tickDelta) {
        return Mth.lerp(tickDelta, previousZoom, currentZoom);
    }

    /**
     * Returns the current FOV given the tick delta.
     */
    public Double getFov(float tickDelta) {
        if (previousFov <= -1 || startFov <= -1) return null;
        return Mth.lerp(tickDelta, previousFov, currentFov);
    }

    /**
     * Returns whether the client is currently locked out from changing their zoom level.
     */
    public boolean shouldLockClientFov() {
        return isRegistered() && lockClientFov;
    }

    /**
     * Returns whether the hand should be kept stationary while zooming.
     */
    public boolean isKeepHandStationary() {
        return keepHandStationary;
    }

    /**
     * Applies the given zoom settings.
     */
    public void applyZoom(
            float zoom, int transitionTicks, EasingType easingType, boolean keepHandStationary, Float targetFov) {
        this.keepHandStationary = keepHandStationary;
        this.intendEditFov = targetFov != null;
        this.resetting = false;

        if (transitionTicks <= 0) {
            if (targetFov != null) {
                this.startFov = targetFov;
                this.currentFov = this.startFov;
                this.targetFov = this.startFov;
                this.lockClientFov = true;
            } else {
                this.startFov = -1;
                this.currentFov = -1;
                this.targetFov = -1;
                this.lockClientFov = false;
            }
            this.currentZoom = zoom;
            this.startZoom = zoom;
            this.targetZoom = zoom;
            this.transitionTicksTotal = 0;
            this.transitionTicksRemaining = 0;
            this.easingType = easingType;
            return;
        }

        if (targetFov != null) {
            if (this.startFov == -1) {
                // Determine the current FOV that is actually being rendered!
                var gameRenderer = Minecraft.getInstance().gameRenderer;
                this.startFov = Minecraft.getInstance().options.fov().get();
                this.startFov *= gameRenderer.fovModifier;
                this.previousFov = this.startFov;
                this.currentFov = this.startFov;
            } else {
                this.startFov = this.currentFov;
            }
            this.targetFov = targetFov;
            this.lockClientFov = true;
        } else if (currentFov > -1.0) {
            // Determine the intended fov and go back to it!
            var gameRenderer = Minecraft.getInstance().gameRenderer;
            float fov = Minecraft.getInstance().options.fov().get();
            fov *= gameRenderer.fovModifier;
            this.startFov = this.currentFov;
            this.targetFov = fov;
            this.lockClientFov = true;
        }

        this.startZoom = this.currentZoom;
        this.targetZoom = zoom;
        this.transitionTicksTotal = transitionTicks;
        this.transitionTicksRemaining = transitionTicks;
        this.easingType = easingType;
    }

    /**
     * Resets the zoom overrides back to defaults.
     */
    public void reset(Optional<Integer> resetTicks, EasingType easingType) {
        if (resetTicks.isPresent() && resetTicks.get() > 0) {
            this.resetting = true;

            // Determine the intended fov and go back to it!
            var gameRenderer = Minecraft.getInstance().gameRenderer;
            float fov = Minecraft.getInstance().options.fov().get();
            fov *= gameRenderer.fovModifier;

            // Start zooming towards the default values
            applyZoom(1.0f, resetTicks.get(), easingType, this.keepHandStationary, fov);
            return;
        }

        this.currentZoom = 1.0f;
        this.startZoom = 1.0f;
        this.targetZoom = 1.0f;
        this.transitionTicksTotal = 0;
        this.transitionTicksRemaining = 0;
        this.startFov = -1;
        this.currentFov = -1;
        this.targetFov = -1;
        this.resetting = false;
        this.lockClientFov = false;
        this.intendEditFov = false;
        this.keepHandStationary = false;
        this.easingType = EasingType.LINEAR;
    }

    /**
     * Ticks the current zoom level.
     */
    private void tick() {
        previousZoom = currentZoom;
        previousFov = currentFov;

        // If we've reached the end jump to the target values!
        if (transitionTicksRemaining-- <= 0 || transitionTicksTotal <= 0) {
            currentZoom = targetZoom;
            currentFov = targetFov;

            // If we didn't intend to edit the FOV, reset it when we're done with this transition!
            if (!intendEditFov) {
                startFov = -1;
                currentFov = -1;
                targetFov = -1;
                lockClientFov = false;
            }

            if (resetting) {
                reset(Optional.empty(), EasingType.LINEAR);
            }
            return;
        }

        // Calculate the current zoom and FOV values!
        var total = (float) transitionTicksTotal;
        var elapsed = (float) (transitionTicksTotal - transitionTicksRemaining);
        var eased = (float) easingType.apply(elapsed / total);

        currentZoom = startZoom + (targetZoom - startZoom) * eased;
        if (startFov > -1) {
            currentFov = startFov + (targetFov - startFov) * eased;
        }
    }
}
