package com.noxcrew.noxesium.core.fabric.mixin.feature.ext;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererExt<T extends Entity, S extends EntityRenderState> {
    @Invoker("getBoundingBoxForCulling")
    AABB invokeGetBoundingBoxForCulling(T entity);
}
