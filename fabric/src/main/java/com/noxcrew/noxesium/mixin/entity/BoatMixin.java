package com.noxcrew.noxesium.mixin.entity;

import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Boat.class)
public class BoatMixin {

    @Inject(method = "canCollideWith", at = @At(value = "HEAD"), cancellable = true)
    public void injected(Entity entity, CallbackInfoReturnable<Boolean> ci) {
        if (ServerRules.DISABLE_BOAT_COLLISIONS.getValue()) {
            ci.setReturnValue(false);
        }
    }

}
