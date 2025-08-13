package com.noxcrew.noxesium.fabric.feature.rule.impl;

import com.noxcrew.noxesium.fabric.mixin.rules.mouse.MouseHandlerExt;
import java.util.Objects;
import net.minecraft.client.Minecraft;

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
            var mouseHandler = (MouseHandlerExt) Minecraft.getInstance().mouseHandler;
            mouseHandler.setAccumulatedDeltaX(0.0);
            mouseHandler.setAccumulatedDeltaY(0.0);
        }
    }
}
