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

    @Unique
    private boolean noxesium$initialized = false;

    @Shadow
    public abstract AABB getBoundingBox();

    @Inject(method = "setId", at = @At("RETURN"))
    public void setId(int i, CallbackInfo ci) {
        // When the entity id is set we trigger a spatial container update!
        if (((Object) this) instanceof Interaction interaction) {
            noxesium$initialized = true;
            SpatialInteractionEntityTree.update(interaction);
        }
    }

    @Inject(method = "setBoundingBox", at = @At("HEAD"))
    public void onUpdateBoundingBox(AABB aABB, CallbackInfo ci) {
        // Ignore updates until the entity id has been set
        if (!noxesium$initialized) return;

        // Ignore if we're already at the exact same position!
        if (((Object) this) instanceof Interaction interaction && !aABB.equals(getBoundingBox())) {
            SpatialInteractionEntityTree.update(interaction);
        }
    }

    @Inject(method = "setRemoved", at = @At("RETURN"))
    public void onRemoved(Entity.RemovalReason removalReason, CallbackInfo ci) {
        // Ignore updates until the entity id has been set
        if (!noxesium$initialized) return;

        if (((Object) this) instanceof Interaction interaction) {
            SpatialInteractionEntityTree.remove(interaction);
        }
    }
}
