package com.noxcrew.noxesium.feature.entity;

import com.noxcrew.noxesium.NoxesiumModule;

/**
 * Helps in debugging the spatial interaction tree.
 */
public class SpatialDebuggingModule implements NoxesiumModule {

    @Override
    public void onStartup() {
        // NoxesiumMod.getPlatform().registerRenderHook(this::onRenderHook);
    }

    // TODO Re-implement in the future!

    /*private void onRenderHook() {
        if (!NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging) return;

        // Don't show this view when rendering hitboxes!
        if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) return;

        var models = SpatialInteractionEntityTree.getModelContents();

        Profiler.get().push("noxesium-debug");
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(true);

        final var oldShader = RenderSystem.getShader();
        RenderSystem.setShader(CoreShaders.RENDERTYPE_LINES);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        var vec3 = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        var poseStack = new PoseStack();
        poseStack.translate(-vec3.x, -vec3.y, -vec3.z);

        var color = new Color(255, 214, 31);

        for (var model : models) {
            RenderSystem.lineWidth(2.0f);
            RenderSystem.depthFunc(GL32.GL_ALWAYS);

            // Start the buffer after setting up the depth settings
            var buffer =
                    Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            try {
                ShapeRenderer.renderLineBox(
                        poseStack,
                        buffer,
                        model,
                        color.getRed() / 255f,
                        color.getGreen() / 255f,
                        color.getBlue() / 255f,
                        1.0F);
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
        RenderSystem.setShader(oldShader);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();

        Profiler.get().pop();
    }*/
}
