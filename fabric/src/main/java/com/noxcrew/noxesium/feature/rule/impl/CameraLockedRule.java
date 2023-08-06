package com.noxcrew.noxesium.feature.rule.impl;

import com.noxcrew.noxesium.mixin.mouse.MouseHandlerAccessor;
import net.minecraft.client.Minecraft;

import java.util.Objects;

/**
 * A special type of boolean rule that resets the accumulated mouse
 * movement when the value is set to false.
 */
public class CameraLockedRule extends BooleanServerRule {

    public CameraLockedRule(int index) {
        super(index, false);
    }

    @Override
    protected void onValueChanged(Boolean oldValue, Boolean newValue) {
        // Using comparison here to avoid unboxing since the booleans are nullable
        if (Objects.equals(oldValue, true) && !Objects.equals(newValue, true)) {
            // Remove all accumulated mouse movement whenever the camera stops being locked
            var mouseHandler = (MouseHandlerAccessor) Minecraft.getInstance().mouseHandler;
            mouseHandler.setAccumulatedDeltaX(0.0);
            mouseHandler.setAccumulatedDeltaY(0.0);
        }
    }
}
