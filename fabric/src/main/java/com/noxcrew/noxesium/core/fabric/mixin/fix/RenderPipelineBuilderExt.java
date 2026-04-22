package com.noxcrew.noxesium.core.fabric.mixin.fix;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPipeline.Builder.class)
public interface RenderPipelineBuilderExt {
    @Accessor("location")
    Optional<Identifier> getLocation();
}
