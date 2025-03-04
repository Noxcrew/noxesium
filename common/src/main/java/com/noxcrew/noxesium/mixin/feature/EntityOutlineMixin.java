package com.noxcrew.noxesium.mixin.feature;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.mixin.feature.ext.EntityRendererExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityOutlineMixin {

    @Redirect(
            method =
                    "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
            at =
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/renderer/entity/state/EntityRenderState;isInvisible:Z",
                            opcode = Opcodes.GETFIELD,
                            ordinal = 1))
    private <E extends Entity, S extends EntityRenderState> boolean render(S instance) {
        if (NoxesiumMod.getInstance().getConfig().showCullingBoxes) {
            return false;
        }
        return instance.isInvisible;
    }

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
