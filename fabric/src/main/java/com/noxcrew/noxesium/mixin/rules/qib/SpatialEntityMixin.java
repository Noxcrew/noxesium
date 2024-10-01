package com.noxcrew.noxesium.mixin.rules.qib;

import com.noxcrew.noxesium.feature.entity.SpatialInteractionEntityTree;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds logic to add interaction entities to the spatial container.
 */
@Mixin(Entity.class)
public abstract class SpatialEntityMixin {

    @Shadow
    public abstract AABB getBoundingBox();

    @Inject(method = "setBoundingBox", at = @At("HEAD"))
    public void onUpdateBoundingBox(AABB aABB, CallbackInfo ci) {
        // Ignore if we're already at the exact same position!
        if (((Object) this) instanceof Interaction interaction && !aABB.equals(getBoundingBox())) {
            SpatialInteractionEntityTree.update(interaction);
        }
    }

    @Inject(method = "setRemoved", at = @At("RETURN"))
    public void onRemoved(Entity.RemovalReason removalReason, CallbackInfo ci) {
        if (((Object) this) instanceof Interaction interaction) {
            SpatialInteractionEntityTree.remove(interaction);
        }
    }
}
