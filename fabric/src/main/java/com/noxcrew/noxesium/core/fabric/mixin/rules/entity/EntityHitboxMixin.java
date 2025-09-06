package com.noxcrew.noxesium.core.fabric.mixin.rules.entity;

import com.noxcrew.noxesium.core.fabric.feature.entity.HitboxHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Reads the hitbox of an entity from its hitbox override.
 */
@Mixin(Entity.class)
public class EntityHitboxMixin {
    @Inject(
            method = "makeBoundingBox(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;",
            at = @At("HEAD"),
            cancellable = true)
    public void makeBoundingBox(Vec3 position, CallbackInfoReturnable<AABB> cir) {
        var override = HitboxHelper.getBoundingBox((Entity) ((Object) this), position);
        if (override != null) {
            cir.setReturnValue(override);
        }
    }
}
