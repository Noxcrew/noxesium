package com.noxcrew.noxesium.core.fabric.mixin.feature.config;

import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class DebugEntityHitboxMixin {

    @Redirect(
            method = "emitGizmos",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInvisible()Z"))
    private boolean render(Entity instance) {
        if (NoxesiumMod.getInstance().getConfig().showCullingBoxes()) {
            return false;
        }
        return instance.isInvisible();
    }

    @Redirect(
            method = "showHitboxes",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
    private <T extends Entity> AABB getBoundingBox(T instance) {
        if (NoxesiumMod.getInstance().getConfig().showCullingBoxes()) {
            return ((EntityRendererExt<T, ?>)
                            Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(instance))
                    .invokeGetBoundingBoxForCulling(instance);
        }
        return instance.getBoundingBox();
    }
}
