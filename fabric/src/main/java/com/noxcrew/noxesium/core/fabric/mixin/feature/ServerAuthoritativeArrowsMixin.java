package com.noxcrew.noxesium.core.fabric.mixin.feature;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows arrows with flag 6 to be server authoritative movement
 * controlled which means the client does not attempt to make any
 * adjustments to the velocity or deflect the arrow. This does
 * mean the arrow has a delayed reaction when it does get deflected
 * but this may be desired behavior for servers that want to cancel
 * arrow collisions and generally not deflect.
 */
@Mixin(AbstractArrow.class)
public class ServerAuthoritativeArrowsMixin {

    /**
     * Returns whether the given arrow uses server authoritative movement.
     */
    @Unique
    public boolean noxesium$isServerAuthoritative() {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        var value = arrow.getEntityData().get(AbstractArrow.ID_FLAGS);
        return (value & 64) != 0;
    }

    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    public void onHitEntity(EntityHitResult p_36757_, CallbackInfo ci) {
        if (noxesium$isServerAuthoritative()) {
            ci.cancel();
        }
    }
}
