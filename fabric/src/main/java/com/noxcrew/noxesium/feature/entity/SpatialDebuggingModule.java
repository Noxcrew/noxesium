package com.noxcrew.noxesium.feature.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.NoxesiumModule;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL32;

import java.awt.Color;

/**
 * Helps in debugging the spatial interaction tree.
 */
public class SpatialDebuggingModule implements NoxesiumModule {

    @Override
    public void onStartup() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> {
            // If Fabulous mode is used we want to render earlier!
            if (ctx.advancedTranslucency()) {
                onRenderHook();
            }
        });

        WorldRenderEvents.LAST.register(ctx -> {
            // If not using Fabulous we render last
            if (!ctx.advancedTranslucency()) {
                onRenderHook();
            }
        });
    }

    private void onRenderHook() {
        if (!NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging) return;

        // Don't show this view when rendering hitboxes!
        if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) return;

        var models = SpatialInteractionEntityTree.getModelContents();

        Minecraft.getInstance().getProfiler().push("noxesium-debug");
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(true);

        final ShaderInstance oldShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        var vec3 = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        var poseStack = new PoseStack();
        poseStack.translate(-vec3.x, -vec3.y, -vec3.z);

        var color = new Color(255, 214, 31);

        for (var model : models) {
            RenderSystem.lineWidth(2.0f);
            RenderSystem.depthFunc(GL32.GL_ALWAYS);

            // Start the buffer after setting up the depth settings
            var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            try {
                LevelRenderer.renderLineBox(poseStack, buffer, model, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1.0F);
            } catch (Exception x) {
                // Ignore exceptions from in here
                if (SharedConstants.IS_RUNNING_IN_IDE) throw x;
            } finally {
                var meshdata = buffer.build();
                if (meshdata != null) {
                    BufferUploader.drawWithShader(meshdata);
                }
            }
        }

        RenderSystem.depthFunc(GL32.GL_LEQUAL);
        RenderSystem.setShader(() -> oldShader);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();

        Minecraft.getInstance().getProfiler().pop();
    }
}
