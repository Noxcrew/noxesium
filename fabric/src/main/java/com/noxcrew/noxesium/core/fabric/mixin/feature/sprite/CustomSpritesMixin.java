package com.noxcrew.noxesium.core.fabric.mixin.feature.sprite;

import com.noxcrew.noxesium.core.fabric.feature.sprite.SkullFontDescription;
import com.noxcrew.noxesium.core.fabric.feature.sprite.SkullSprite;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.network.chat.FontDescription;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Modifies the font manager's code for fetching the glyph source of a font.
 */
@Mixin(FontManager.CachedFontProvider.class)
public abstract class CustomSpritesMixin {

    @Inject(method = "getGlyphSource", at = @At("HEAD"), cancellable = true)
    private void getGlyphSource(FontDescription fontDescription, CallbackInfoReturnable<GlyphSource> cir) {
        if (fontDescription instanceof SkullFontDescription font) {
            cir.setReturnValue(SkullSprite.GLYPH_SOURCE_CACHE.getUnchecked(font.sprite()));
        }
    }
}
