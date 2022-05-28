package com.noxcrew.noxesium.mixin.client.beacon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hooks into the LevelRenderer's logic for calling draw calls for block entities and replaces
 * it completely with a segment where all beacons are rendered together.
 */
@Mixin(LevelRenderer.class)
public class BeaconRenderingMixin {

    @Shadow
    @Final
    private Set<BlockEntity> globalBlockEntities;

    @Shadow
    @Final
    private BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    /*
     * We need to replace the rendering of global entities. However we can't replace a synchronized block, so
     * instead we'll modify the variable to make it loop through an empty list. Then we add an inject right after.
     */

    @Redirect(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;globalBlockEntities:Ljava/util/Set;", opcode = Opcodes.GETFIELD, ordinal = 1))
    private Set<BlockEntity> getGlobalBlockEntities(LevelRenderer instance) {
        return Collections.emptySet();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 1, shift = At.Shift.BEFORE))
    private void renderGlobalBlockEntities(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        var bufferSource = renderBuffers.bufferSource();
        var vec3 = camera.getPosition();

        synchronized (globalBlockEntities) {
            // Separately do all beacons at once
            var beacons = new HashSet<Pair<BeaconBlockEntity, BlockEntityRenderer<BeaconBlockEntity>>>();

            for (BlockEntity blockEntity : globalBlockEntities) {
                // If this is a beacon do pre-processing to find if we should attempt to render it
                if (blockEntity instanceof BeaconBlockEntity beacon) {
                    BlockEntityRenderer<BeaconBlockEntity> blockEntityRenderer = blockEntityRenderDispatcher.getRenderer(beacon);
                    if (blockEntityRenderer != null) {
                        if (blockEntity.hasLevel() && blockEntity.getType().isValid(blockEntity.getBlockState())) {
                            if (blockEntityRenderer.shouldRender(beacon, camera.getPosition())) {
                                // Ignore beacons with no beam sections
                                if (!beacon.getBeamSections().isEmpty()) {
                                    beacons.add(Pair.of(beacon, blockEntityRenderer));
                                }
                            }
                        }
                    }
                    continue;
                }

                // Otherwise render it like normal
                var outlineBufferSource = blockEntity.getBlockPos();
                poseStack.pushPose();
                poseStack.translate((double) ((Vec3i) outlineBufferSource).getX() - vec3.x, (double) ((Vec3i) outlineBufferSource).getY() - vec3.y, (double) ((Vec3i) outlineBufferSource).getZ() - vec3.z);
                blockEntityRenderDispatcher.render(blockEntity, f, poseStack, bufferSource);
                poseStack.popPose();
            }

            // If there are no beacons we don't need to do anything
            if (beacons.isEmpty()) return;

            // All beacons are centered on the block
            var resourceLocation = BeaconRenderer.BEAM_LOCATION;
            poseStack.pushPose();
            poseStack.translate(0.5, 0.0, 0.5);

            // Go through each beacon only setting up the two buffer types once
            {
                var renderType = RenderType.beaconBeam(resourceLocation, false);
                var buffer = bufferSource.getBuffer(renderType);
                for (Pair<BeaconBlockEntity, BlockEntityRenderer<BeaconBlockEntity>> data : beacons) {
                    var blockEntity = data.getLeft();
                    var outlineBufferSource = blockEntity.getBlockPos();
                    poseStack.pushPose();
                    poseStack.translate((double) outlineBufferSource.getX() - vec3.x, (double) outlineBufferSource.getY() - vec3.y, (double) outlineBufferSource.getZ() - vec3.z);
                    renderNonTransparent(blockEntity, f, poseStack, buffer);
                    poseStack.popPose();
                }
                bufferSource.endBatch(renderType);
            }
            {
                var renderType = RenderType.beaconBeam(resourceLocation, true);
                var buffer = bufferSource.getBuffer(renderType);
                for (Pair<BeaconBlockEntity, BlockEntityRenderer<BeaconBlockEntity>> data : beacons) {
                    var blockEntity = data.getLeft();
                    var outlineBufferSource = blockEntity.getBlockPos();
                    poseStack.pushPose();
                    poseStack.translate((double) outlineBufferSource.getX() - vec3.x, (double) outlineBufferSource.getY() - vec3.y, (double) outlineBufferSource.getZ() - vec3.z);
                    renderTransparent(blockEntity, f, poseStack, buffer);
                    poseStack.popPose();
                }
                bufferSource.endBatch(renderType);
            }

            poseStack.popPose();
        }
    }

    private static void renderNonTransparent(BeaconBlockEntity beaconBlockEntity, float f, PoseStack poseStack, VertexConsumer buffer) {
        long l = beaconBlockEntity.getLevel().getGameTime();
        List<BeaconBlockEntity.BeaconBeamSection> list = beaconBlockEntity.getBeamSections();
        int i = 0;
        for (int sectionIndex = 0; sectionIndex < list.size(); ++sectionIndex) {
            BeaconBlockEntity.BeaconBeamSection beaconBeamSection = list.get(sectionIndex);
            var g = 1.0f;
            var j = sectionIndex == list.size() - 1 ? BeaconRenderer.MAX_RENDER_Y : beaconBeamSection.getHeight();
            var fs = beaconBeamSection.getColor();
            var h = 0.2f;
            int m = i + j;

            float n = (float) Math.floorMod(l, 40) + f;
            float o = j < 0 ? n : -n;
            float p = Mth.frac(o * 0.2f - (float) Mth.floor(o * 0.1f));
            float q = fs[0];
            float r = fs[1];
            float s = fs[2];
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(n * 2.25f - 45.0f));
            float ad = -1.0f + p;
            float ae = (float) j * g * (0.5f / h) + ad;
            renderPart(poseStack, buffer, q, r, s, 1.0f, i, m, 0.0f, h, h, 0.0f, -h, 0.0f, 0.0f, -h, 0.0f, 1.0f, ae, ad);
            poseStack.popPose();

            i += beaconBeamSection.getHeight();
        }
    }

    private static void renderTransparent(BeaconBlockEntity beaconBlockEntity, float f, PoseStack poseStack, VertexConsumer buffer) {
        long l = beaconBlockEntity.getLevel().getGameTime();
        List<BeaconBlockEntity.BeaconBeamSection> list = beaconBlockEntity.getBeamSections();
        int i = 0;
        for (int sectionIndex = 0; sectionIndex < list.size(); ++sectionIndex) {
            BeaconBlockEntity.BeaconBeamSection beaconBeamSection = list.get(sectionIndex);
            var g = 1.0f;
            var j = sectionIndex == list.size() - 1 ? BeaconRenderer.MAX_RENDER_Y : beaconBeamSection.getHeight();
            var fs = beaconBeamSection.getColor();
            var k = 0.25f;
            int m = i + j;

            float n = (float) Math.floorMod(l, 40) + f;
            float o = j < 0 ? n : -n;
            float p = Mth.frac(o * 0.2f - (float) Mth.floor(o * 0.1f));
            float q = fs[0];
            float r = fs[1];
            float s = fs[2];
            float ad = -1.0f + p;
            float ae = (float) j * g + ad;
            renderPart(poseStack, buffer, q, r, s, 0.125f, i, m, -k, -k, k, -k, -k, k, k, k, 0.0f, 1.0f, ae, ad);

            i += beaconBeamSection.getHeight();
        }
    }

    private static void renderPart(PoseStack poseStack, VertexConsumer vertexConsumer, float f, float g, float h, float i, int j, int k, float l, float m, float n, float o, float p, float q, float r, float s, float t, float u, float v, float w) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        renderQuad(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, l, m, n, o, t, u, v, w);
        renderQuad(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, r, s, p, q, t, u, v, w);
        renderQuad(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, n, o, r, s, t, u, v, w);
        renderQuad(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, p, q, l, m, t, u, v, w);
    }

    private static void renderQuad(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float f, float g, float h, float i, int j, int k, float l, float m, float n, float o, float p, float q, float r, float s) {
        addVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, k, l, m, q, r);
        addVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, l, m, q, s);
        addVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, n, o, p, s);
        addVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, k, n, o, p, r);
    }

    private static void addVertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float f, float g, float h, float i, int j, float k, float l, float m, float n) {
        vertexConsumer.vertex(matrix4f, k, j, l).color(f, g, h, i).uv(m, n).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(matrix3f, 0.0f, 1.0f, 0.0f).endVertex();
    }
}
