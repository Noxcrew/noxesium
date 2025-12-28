package com.noxcrew.noxesium.core.fabric.mixin.settings;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.ToggleKeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fixes MC-301281 which is an issue where toggle keys are not re-enabled
 * after closing a menu if it's not a keyboard button.
 */
@Mixin(ToggleKeyMapping.class)
public abstract class FixMouseToggleKeys {

    @WrapOperation(
            method = "shouldRestoreStateOnScreenClosed",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/blaze3d/platform/InputConstants$Key;getType()Lcom/mojang/blaze3d/platform/InputConstants$Type;"))
    public InputConstants.Type onResetToggleKeys(InputConstants.Key instance, Operation<InputConstants.Type> original) {
        return InputConstants.Type.KEYSYM;
    }
}
