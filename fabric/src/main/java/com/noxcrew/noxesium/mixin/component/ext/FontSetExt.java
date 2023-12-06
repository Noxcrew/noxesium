package com.noxcrew.noxesium.mixin.component.ext;

import com.mojang.blaze3d.font.SheetGlyphInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FontSet.class)
public interface FontSetExt {

    @Accessor("glyphsByWidth")
    Int2ObjectMap<IntList> getGlyphsByWidth();

    @Accessor("missingGlyph")
    BakedGlyph getMissingGlyph();

    @Invoker("stitch")
    BakedGlyph invokeStitch(SheetGlyphInfo sheetGlyphInfo);
}
