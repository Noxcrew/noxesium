package com.noxcrew.noxesium.mixin.beacon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noxcrew.noxesium.render.GlobalBlockEntityRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * Hooks into the LevelRenderer's logic for calling draw calls for block entities and replaces
 * it completely with a segment where all beacons are rendered together.
 */
@Mixin(LevelRenderer.class)
public class BeaconRenderingMixin {

    private static final Set<BlockEntity> EMPTY_SET = Set.of();

    @Shadow
    @Final
    private Set<BlockEntity> globalBlockEntities;

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Redirect(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;globalBlockEntities:Ljava/util/Set;", opcode = Opcodes.GETFIELD, ordinal = 1))
    private Set<BlockEntity> getGlobalBlockEntities(LevelRenderer instance) {
        // Return nothing to the default renderer of global block entities so we can do it custom
        return EMPTY_SET;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 1, shift = At.Shift.BEFORE))
    private void renderGlobalBlockEntities(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        // Synchronise on the global block entities because vanilla does as well, this list will be empty
        // when using Sodium
        synchronized (globalBlockEntities) {
            GlobalBlockEntityRenderer.render(globalBlockEntities, camera, poseStack, renderBuffers.bufferSource(), camera.getPosition(), f);
        }
    }
}
