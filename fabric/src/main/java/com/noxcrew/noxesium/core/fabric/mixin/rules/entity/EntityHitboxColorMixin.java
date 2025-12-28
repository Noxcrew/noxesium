package com.noxcrew.noxesium.core.fabric.mixin.rules.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Changes the color of an entity's hitbox.
 */
@Mixin(EntityHitboxDebugRenderer.class)
public class EntityHitboxColorMixin {

    @WrapOperation(
            method = "showHitboxes",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/gizmos/GizmoStyle;stroke(I)Lnet/minecraft/gizmos/GizmoStyle;"))
    public GizmoStyle redirectRenderHitbox(
            int base, Operation<GizmoStyle> original, @Local(argsOnly = true) Entity entity) {
        var optionalColor = entity.noxesium$getOptionalComponent(CommonEntityComponentTypes.HITBOX_COLOR);
        if (optionalColor.isPresent()) {
            var color = optionalColor.get();
            return original.call(ARGB.color(color.getRed(), color.getGreen(), color.getBlue()));
        }
        return original.call(base);
    }
}
