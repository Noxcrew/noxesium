package com.noxcrew.noxesium.mixin.ui.ext;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.vertex.VertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Grants access to the GPU vertex buffer.
 */
@Mixin(VertexBuffer.class)
public interface VertexBufferExt {

    @Accessor("vertexBuffer")
    GpuBuffer getGpuBuffer();
}

