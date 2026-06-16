package com.noxcrew.noxesium.core.fabric.mixin.feature.debugoptions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.client.gui.components.debug.DebugEntryPosition;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DebugEntryPosition.class)
public class RestrictDebugPositionMixin {
    @WrapOperation(
            method = "display",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getYRot()F"))
    public float getYRot(Entity instance, Operation<Float> original) {
        if (GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.DEBUG_POSITION_HIDE_YAW))
            return 0f;
        return original.call(instance);
    }

    @WrapOperation(
            method = "display",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getXRot()F"))
    public float getXRot(Entity instance, Operation<Float> original) {
        if (GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.DEBUG_POSITION_HIDE_PITCH))
            return 0f;
        return original.call(instance);
    }
}
