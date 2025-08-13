package com.noxcrew.noxesium.fabric.feature.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.profiling.Profiler;

/**
 * Helps in debugging the spatial interaction tree.
 */
public class SpatialDebuggingRenderer implements DebugRenderer.SimpleDebugRenderer {

    @Override
    public void render(
            PoseStack poseStack, MultiBufferSource multiBufferSource, double cameraX, double cameraY, double cameraZ) {
        // Don't show this view when rendering hitboxes!
        if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) return;

        Profiler.get().push("noxesium-debug");
        var models = SpatialInteractionEntityTree.getModelContents();
        var color = new Color(255, 214, 31);
        var vertexconsumer = multiBufferSource.getBuffer(RenderType.debugLineStrip(2.0));
        poseStack.pushPose();
        poseStack.translate(-cameraX, -cameraY, -cameraZ);
        for (var model : models) {
            ShapeRenderer.renderLineBox(
                    poseStack,
                    vertexconsumer,
                    model,
                    color.getRed() / 255f,
                    color.getGreen() / 255f,
                    color.getBlue() / 255f,
                    1.0F);
        }
        poseStack.popPose();
        Profiler.get().pop();
    }
}
