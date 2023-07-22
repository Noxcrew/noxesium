package com.noxcrew.noxesium.mixin.beacon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noxcrew.noxesium.render.GlobalBlockEntityRenderer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.SortedSet;

/**
 * Hooks into Sodium's WorldRenderer logic for rendering global block entities (which all beacons are)
 * and replaces it with the custom beacon rendering logic.
 */
@Mixin(SodiumWorldRenderer.class)
public class SodiumWorldRendererMixin {

    private static final Set<BlockEntity> EMPTY_SET = Set.of();

    @Shadow
    @Final
    private Set<BlockEntity> globalBlockEntities;

    @Redirect(method = "renderTileEntities", at = @At(value = "FIELD", target = "Lme/jellysquid/mods/sodium/client/render/SodiumWorldRenderer;globalBlockEntities:Ljava/util/Set;", opcode = Opcodes.GETFIELD))
    private Set<BlockEntity> removeBuiltinRenderGlobal(SodiumWorldRenderer instance) {
        // Return nothing to the default renderer of global block entities so we can do it custom
        return EMPTY_SET;
    }

    @Inject(method = "renderTileEntities", at = @At(value = "TAIL"))
    private void renderGlobalBlockEntities(PoseStack poseStack, RenderBuffers renderBuffers, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, Camera camera, float tickDelta, CallbackInfo ci) {
        GlobalBlockEntityRenderer.render(globalBlockEntities, camera, poseStack, renderBuffers.bufferSource(), camera.getPosition(), tickDelta);
    }
}
