package com.noxcrew.noxesium.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.noxcrew.noxesium.mixin.performance.ext.BeaconRendererExt;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Handles rendering logic for global block entities, specifically optimizes beacon rendering.
 */
public class GlobalBlockEntityRenderer {
    public static void render(Collection<BlockEntity> globalBlockEntities, Camera camera, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vec3 cameraPosition, DeltaTracker deltaTracker) {
        if (globalBlockEntities.isEmpty()) return;

        var tickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);
        var blockEntityRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        var cameraX = cameraPosition.x;
        var cameraY = cameraPosition.y;
        var cameraZ = cameraPosition.z;

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

            // Otherwise render it like normal (this is only ever used for structure blocks)
            var outlineBufferSource = blockEntity.getBlockPos();
            poseStack.pushPose();
            poseStack.translate(outlineBufferSource.getX() - cameraX, outlineBufferSource.getY() - cameraY, outlineBufferSource.getZ() - cameraZ);
            blockEntityRenderDispatcher.render(blockEntity, tickDelta, poseStack, bufferSource);
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
                poseStack.translate(outlineBufferSource.getX() - cameraX, outlineBufferSource.getY() - cameraY, outlineBufferSource.getZ() - cameraZ);
                renderNonTransparent(blockEntity, tickDelta, poseStack, buffer);
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
                poseStack.translate(outlineBufferSource.getX() - cameraX, outlineBufferSource.getY() - cameraY, outlineBufferSource.getZ() - cameraZ);
                renderTransparent(blockEntity, tickDelta, poseStack, buffer);
                poseStack.popPose();
            }
            bufferSource.endBatch(renderType);
        }

        poseStack.popPose();
    }

    private static void renderNonTransparent(BeaconBlockEntity beaconBlockEntity, float tickDelta, PoseStack poseStack, VertexConsumer buffer) {
        var h = 0.2f;
        renderElement(beaconBlockEntity, tickDelta, poseStack, buffer, true, 0.0f, h, h, 0.0f, -h, 0.0f, 0.0f, -h, 0.5f / h);
    }

    private static void renderTransparent(BeaconBlockEntity beaconBlockEntity, float tickDelta, PoseStack poseStack, VertexConsumer buffer) {
        var k = 0.25f;
        renderElement(beaconBlockEntity, tickDelta, poseStack, buffer, false, -k, -k, k, -k, -k, k, k, k, 1.0f);
    }

    private static void renderElement(BeaconBlockEntity beaconBlockEntity, float tickDelta, PoseStack poseStack, VertexConsumer buffer, boolean spin, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11) {
        long l = beaconBlockEntity.getLevel().getGameTime();
        List<BeaconBlockEntity.BeaconBeamSection> list = beaconBlockEntity.getBeamSections();
        int i = 0;
        for (int sectionIndex = 0; sectionIndex < list.size(); ++sectionIndex) {
            BeaconBlockEntity.BeaconBeamSection beaconBeamSection = list.get(sectionIndex);
            var j = sectionIndex == list.size() - 1 ? BeaconRenderer.MAX_RENDER_Y : beaconBeamSection.getHeight();

            float o = (float)Math.floorMod(l, 40) + tickDelta;
            float p = j < 0 ? o : -o;
            float q = Mth.frac(p * 0.2F - (float)Mth.floor(p * 0.1F));

            if (spin) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(o * 2.25f - 45.0f));
            }
            float ab = -1.0f + q;
            float ac = (float)j * f11 + ab;
            int color = spin ? beaconBeamSection.getColor() : FastColor.ARGB32.color(32, beaconBeamSection.getColor());
            BeaconRendererExt.invokeRenderPart(poseStack, buffer, color, i, i + j, f3, f4, f5, f6, f7, f8, f9, f10, 0.0f, 1.0f, ac, ab);
            if (spin) {
                poseStack.popPose();
            }

            i += beaconBeamSection.getHeight();
        }
    }
}
