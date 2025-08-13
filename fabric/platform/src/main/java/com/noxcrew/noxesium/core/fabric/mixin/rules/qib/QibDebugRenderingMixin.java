package com.noxcrew.noxesium.core.fabric.mixin.rules.qib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.fabric.feature.entity.SpatialInteractionEntityTree;
import com.noxcrew.noxesium.core.fabric.feature.render.CustomRenderTypes;
import com.noxcrew.noxesium.core.fabric.registry.CommonEntityComponentTypes;
import java.awt.Color;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class QibDebugRenderingMixin {

    @Inject(
            method =
                    "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
            at = @At("TAIL"))
    private void render(
            Entity entity,
            double d,
            double e,
            double f,
            float g,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int i,
            EntityRenderer<?, ?> entityRenderer,
            CallbackInfo ci) {
        var entityRenderDispatcher = ((EntityRenderDispatcher) (Object) this);

        if (!NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging) return;
        if (!entityRenderDispatcher.shouldRenderHitBoxes()) return;
        if (Minecraft.getInstance().showOnlyReducedInfo()) return;

        // Detect any interaction entities that are invisible
        if (entity.getType() == EntityType.INTERACTION && entity.isInvisible()) {
            var aabb = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
            if (aabb.hasNaN() || aabb.getSize() == 0.0) return;

            poseStack.pushPose();
            poseStack.translate(d, e, f);

            // Draw a custom hitbox with the color of the associated qib behavior if it has loaded
            var noDepthBuffer = multiBufferSource.getBuffer(CustomRenderTypes.linesNoDepth());
            var qibBehavior = entity.noxesium$getComponent(CommonEntityComponentTypes.QIB_BEHAVIOR);
            if (qibBehavior != null) {
                var seededRandom = new Random(qibBehavior.hashCode());
                var color = new Color(seededRandom.nextInt());
                ShapeRenderer.renderLineBox(
                        poseStack,
                        noDepthBuffer,
                        aabb,
                        color.getRed() / 255f,
                        color.getGreen() / 255f,
                        color.getBlue() / 255f,
                        0.5F);
                var buffer = multiBufferSource.getBuffer(RenderType.lines());
                ShapeRenderer.renderLineBox(
                        poseStack,
                        buffer,
                        aabb,
                        color.getRed() / 255f,
                        color.getGreen() / 255f,
                        color.getBlue() / 255f,
                        1.0F);
            }

            // Draw a name tag based on the state of this entity in the spatial tree
            var state = SpatialInteractionEntityTree.getSpatialTreeState(entity);
            if (state != null) {
                double distance = entityRenderDispatcher.distanceToSqr(entity);
                if (distance <= 4096.0) {
                    var vec3 = new Vec3(0.0, aabb.getYsize() / 2.0, 0.0);
                    if (vec3 != null) {
                        poseStack.pushPose();
                        poseStack.translate(vec3.x, vec3.y, vec3.z);
                        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
                        poseStack.scale(0.025F, -0.025F, 0.025F);
                        var matrix4f = poseStack.last().pose();
                        var font = Minecraft.getInstance().font;
                        var text = Component.literal(state);
                        var dx = (float) (-font.width(text) / 2);
                        font.drawInBatch(
                                text, dx, 0f, -1, false, matrix4f, multiBufferSource, Font.DisplayMode.NORMAL, 0, i);
                        poseStack.popPose();
                    }
                }
            }

            poseStack.popPose();
        }
    }
}
