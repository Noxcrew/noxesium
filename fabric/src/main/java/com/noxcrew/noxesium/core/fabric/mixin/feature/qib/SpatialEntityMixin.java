package com.noxcrew.noxesium.core.fabric.mixin.feature.qib;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.core.fabric.feature.qib.QibBehaviorModule;
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
        if (((Object) this) instanceof Interaction interaction
                &&
                // Ignore non-qibs!
                interaction.noxesium$hasComponent(CommonEntityComponentTypes.QIB_BEHAVIOR)
                &&
                // Ignore if we're already at the exact same position!
                !aABB.equals(getBoundingBox())) {
            NoxesiumApi.getInstance()
                    .getFeatureOptional(QibBehaviorModule.class)
                    .ifPresent(it -> it.getSpatialTree().update(interaction));
        }
    }

    @Inject(method = "setRemoved", at = @At("RETURN"))
    public void onRemoved(Entity.RemovalReason removalReason, CallbackInfo ci) {
        if (((Object) this) instanceof Interaction interaction
                &&
                // Ignore non-qib entities!
                interaction.noxesium$hasComponent(CommonEntityComponentTypes.QIB_BEHAVIOR)) {
            NoxesiumApi.getInstance()
                    .getFeatureOptional(QibBehaviorModule.class)
                    .ifPresent(it -> it.getSpatialTree().remove(interaction));
        }
    }
}
