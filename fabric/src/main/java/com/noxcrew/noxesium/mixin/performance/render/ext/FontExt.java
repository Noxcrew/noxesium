package com.noxcrew.noxesium.mixin.performance.render.ext;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Font.class)
public interface FontExt {

    @Accessor("filterFishyGlyphs")
    boolean getFilterFishyGlyphs();

    @Invoker("getFontSet")
    FontSet invokeGetFontSet(ResourceLocation resourceLocation);

    @Invoker("renderChar")
    void invokeRenderChar(BakedGlyph bakedGlyph, boolean bl, boolean bl2, float f, float g, float h, Matrix4f matrix4f, VertexConsumer vertexConsumer, float i, float j, float k, float l, int m);
}
