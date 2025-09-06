package com.noxcrew.noxesium.core.fabric.mixin.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class EntityOutlineMixin {

    @Shadow
    protected abstract <T extends Entity> AABB getBoundingBoxForCulling(T entity);

    @WrapOperation(
            method = "extractRenderState",
            at =
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/renderer/entity/state/EntityRenderState;isInvisible:Z",
                            opcode = Opcodes.GETFIELD))
    private <S extends EntityRenderState> boolean render(EntityRenderState instance, Operation<Boolean> original) {
        if (NoxesiumMod.getInstance().getConfig().showCullingBoxes) return false;
        return original.call(instance);
    }

    @WrapOperation(
            method =
                    "extractHitboxes(Lnet/minecraft/world/entity/Entity;FZ)Lnet/minecraft/client/renderer/entity/state/HitboxesRenderState;",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
    private <T extends Entity> AABB getBoundingBox(Entity instance, Operation<AABB> original) {
        if (NoxesiumMod.getInstance().getConfig().showCullingBoxes) return getBoundingBoxForCulling(instance);
        return original.call(instance);
    }
}
