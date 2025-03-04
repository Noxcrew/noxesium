package com.noxcrew.noxesium.mixin.feature;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.mixin.feature.ext.EntityRendererExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityOutlineMixin {

    @Redirect(
            method = "renderHitbox",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
    private static AABB getBoundingBox(Entity instance) {
        if (NoxesiumMod.getInstance().getConfig().showCullingBoxes) {
            var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(instance);
            return ((EntityRendererExt) renderer).invokeGetBoundingBoxForCulling(instance);
        }
        return instance.getBoundingBox();
    }
}
