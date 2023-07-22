package com.noxcrew.noxesium.mixin.client.beacon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents beacons from being added to the renderable chunk entities. This is so we can do the
 * entirety of beacon rendering custom for all beacons at once. We do add them to global entities and
 * we catch them from there later.
 *
 * This patch never triggers when Sodium is used.
 */
@Mixin(ChunkRenderDispatcher.RenderChunk.RebuildTask.class)
public class PreventDoubleBeaconRenderingMixin {

    @Inject(method = "handleBlockEntity", at = @At("HEAD"), cancellable = true)
    public <E extends BlockEntity> void render(ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults compileResults, E blockEntity, CallbackInfo ci) {
        if (blockEntity instanceof BeaconBlockEntity) {
            ci.cancel();

            // Only add the beacon to the global entities
            BlockEntityRenderer<E> blockEntityRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
            if (blockEntityRenderer != null) {
                compileResults.globalBlockEntities.add(blockEntity);
            }
        }
    }
}
