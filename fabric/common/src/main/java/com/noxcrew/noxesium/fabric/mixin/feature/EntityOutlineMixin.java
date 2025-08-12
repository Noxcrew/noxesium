package com.noxcrew.noxesium.fabric.mixin.feature;

import com.noxcrew.noxesium.fabric.NoxesiumMod;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public abstract class EntityOutlineMixin {

    @Shadow
    protected abstract <T extends Entity> AABB getBoundingBoxForCulling(T entity);

    @Redirect(
            method = "extractRenderState",
            at =
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/renderer/entity/state/EntityRenderState;isInvisible:Z",
                            opcode = Opcodes.GETFIELD))
    private <S extends EntityRenderState> boolean render(S instance) {
        if (NoxesiumMod.getInstance().getConfig().showCullingBoxes) {
            return false;
        }
        return instance.isInvisible;
    }

    @Redirect(
            method =
                    "extractHitboxes(Lnet/minecraft/world/entity/Entity;FZ)Lnet/minecraft/client/renderer/entity/state/HitboxesRenderState;",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
    private <T extends Entity> AABB getBoundingBox(T instance) {
        if (NoxesiumMod.getInstance().getConfig().showCullingBoxes) {
            return getBoundingBoxForCulling(instance);
        }
        return instance.getBoundingBox();
    }
}
