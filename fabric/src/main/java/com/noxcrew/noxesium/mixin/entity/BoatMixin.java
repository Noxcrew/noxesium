package com.noxcrew.noxesium.mixin.entity;

import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks into the code for boats colliding with other entities to fully
 * disable it whenever the server rule is set.
 * <p>
 * A similar server-side patch is required for it to be fully usable, for example:
 * <a href="https://github.com/TrollsterCooleg/BoatHider/blob/master/BoatHiderNMS_V1_19_R3/src/main/java/me/cooleg/boathider/nms/V1_19_3/CollisionlessBoat.java">BoatHider by TrollsterCooleg</a>
 */
@Mixin(Boat.class)
public class BoatMixin {

    @Inject(method = "canCollideWith", at = @At(value = "HEAD"), cancellable = true)
    public void injected(Entity entity, CallbackInfoReturnable<Boolean> ci) {
        if (ServerRules.DISABLE_BOAT_COLLISIONS.getValue()) {
            ci.setReturnValue(false);
        }
    }

}
