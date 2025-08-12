package com.noxcrew.noxesium.fabric.mixin.feature.component.ext;

import com.mojang.blaze3d.font.SheetGlyphInfo;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FontSet.class)
public interface FontSetExt {

    @Invoker("stitch")
    BakedGlyph invokeStitch(SheetGlyphInfo sheetGlyphInfo);
}
