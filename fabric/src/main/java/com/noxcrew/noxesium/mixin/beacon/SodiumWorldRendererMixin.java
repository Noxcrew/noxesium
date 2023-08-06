package com.noxcrew.noxesium.mixin.beacon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noxcrew.noxesium.render.GlobalBlockEntityRenderer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

/**
 * Hooks into Sodium's WorldRenderer logic for rendering global block entities (which all beacons are)
 * and replaces it with the custom beacon rendering logic.
 */
@Mixin(SodiumWorldRenderer.class)
public class SodiumWorldRendererMixin {

    @Shadow private RenderSectionManager renderSectionManager;

    /**
     * @author Aeltumn
     * @reason Replace normal logic to render all block entities at once instead.
     */
    @Overwrite
    private void renderGlobalBlockEntities(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher blockEntityRenderer) {
        // Remove the normal method. We add a custom call because we need the camera instance.
    }

    @Inject(method = "renderTileEntities", at = @At(value = "TAIL"))
    private void renderGlobalBlockEntities(PoseStack poseStack, RenderBuffers renderBuffers, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, Camera camera, float tickDelta, CallbackInfo ci) {
        Set<BlockEntity> globalBlockEntities = null;
        var iterator = renderSectionManager.getSectionsWithGlobalEntities().iterator();
        outer: while (true) {
            // Find the chunks with section information
            BlockEntity[] blockEntities;
            do {
                // Break the loop when we've reached the end
                if (!iterator.hasNext()) break outer;

                var renderSection = iterator.next();
                blockEntities = renderSection.getGlobalBlockEntities();
            } while (blockEntities == null);

            // Only create the set when we need it
            if (globalBlockEntities == null) {
                globalBlockEntities = new HashSet<>();
            }
            Collections.addAll(globalBlockEntities, blockEntities);
        }

        if (globalBlockEntities == null) return;
        GlobalBlockEntityRenderer.render(globalBlockEntities, camera, poseStack, renderBuffers.bufferSource(), camera.getPosition(), tickDelta);
    }
}