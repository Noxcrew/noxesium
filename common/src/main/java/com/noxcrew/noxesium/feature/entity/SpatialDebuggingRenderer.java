package com.noxcrew.noxesium.feature.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noxcrew.noxesium.NoxesiumModule;
import java.awt.Color;
import java.util.Random;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.util.profiling.Profiler;

/**
 * Helps in debugging the spatial interaction tree.
 */
public class SpatialDebuggingRenderer implements NoxesiumModule, DebugRenderer.SimpleDebugRenderer {

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            double cameraX,
            double cameraY,
            double cameraZ,
            DebugValueAccess debugValueAccess,
            Frustum frustum) {
        Profiler.get().push("noxesium-debug");

        // First render line boxes at render offset
        var models = SpatialInteractionEntityTree.getModelContents();
        var vertexconsumer = multiBufferSource.getBuffer(RenderType.lines());
        poseStack.pushPose();
        poseStack.translate(-cameraX, -cameraY, -cameraZ);
        for (var pair : models) {
            var seededRandom = new Random(pair.getFirst().hashCode());
            var color = new Color(seededRandom.nextInt());
            ShapeRenderer.renderLineBox(
                    poseStack.last(),
                    vertexconsumer,
                    pair.getSecond(),
                    color.getRed() / 255f,
                    color.getGreen() / 255f,
                    color.getBlue() / 255f,
                    1.0F);
        }
        poseStack.popPose();

        // Render floating texts afterwards as they don't need to be offset
        for (var pair : models) {
            // Skip if there is no text!
            if (pair.getFirst().isBlank()) continue;

            var center = pair.getSecond().getCenter();
            DebugRenderer.renderFloatingText(
                    poseStack, multiBufferSource, pair.getFirst(), center.x, center.y, center.z, -1);
        }

        Profiler.get().pop();
    }
}
