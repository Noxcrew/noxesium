package com.noxcrew.noxesium.core.fabric.mixin.rules.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Changes the color of an entity's hitbox.
 */
@Mixin(EntityRenderer.class)
public class EntityHitboxColorMixin<T extends Entity> {

    @WrapOperation(
            method =
                    "extractHitboxes(Lnet/minecraft/world/entity/Entity;FZ)Lnet/minecraft/client/renderer/entity/state/HitboxesRenderState;",
            at = @At(value = "NEW", target = "net/minecraft/client/renderer/entity/state/HitboxRenderState"))
    public HitboxRenderState redirectRenderHitbox(
            double x0,
            double y0,
            double z0,
            double x1,
            double y1,
            double z1,
            float red,
            float green,
            float blue,
            Operation<HitboxRenderState> original,
            @Local(argsOnly = true) T entity) {
        var optionalColor = entity.noxesium$getOptionalComponent(CommonEntityComponentTypes.HITBOX_COLOR);
        if (optionalColor.isPresent()) {
            var color = optionalColor.get();
            red = color.getRed() / 255f;
            green = color.getGreen() / 255f;
            blue = color.getBlue() / 255f;
        }
        return original.call(x0, y0, z0, x1, y1, z1, red, green, blue);
    }
}
