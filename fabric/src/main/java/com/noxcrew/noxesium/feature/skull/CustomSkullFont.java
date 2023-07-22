package com.noxcrew.noxesium.feature.skull;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.noxcrew.noxesium.mixin.client.component.FontSetExt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A custom font that contains glyphs for each skull that needs to be rendered.
 */
public class CustomSkullFont extends FontSet {

    private final SkullFontModule module;
    private NativeImage fallbackGlyph;
    private NativeImage grayscaleFallbackGlyph;

    private final Map<Integer, BakedGlyph> bakedGlyphs = new HashMap<>();
    private final Map<CustomSkullFont.GlyphProperties, BakedGlyph> fallbackBakedGlyphs = new HashMap<>();

    public CustomSkullFont(SkullFontModule module, TextureManager textureManager, ResourceLocation resourceLocation) {
        super(textureManager, resourceLocation);
        this.module = module;

        try {
            ResourceLocation location = DefaultPlayerSkin.getDefaultSkin();
            var simpleTexture = SimpleTexture.TextureImage.load(Minecraft.getInstance().getResourceManager(), location);
            var image = simpleTexture.getImage();

            fallbackGlyph = module.processImage(image, false);
            grayscaleFallbackGlyph = module.processImage(image, true);
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    /**
     * Voids all baked glyphs.
     */
    public void voidBakedGlyphs() {
        bakedGlyphs.clear();
        fallbackBakedGlyphs.clear();
    }

    @Override
    public @NotNull GlyphInfo getGlyphInfo(int i, boolean bl) {
        if (module.getGlyphs().containsKey(i)) {
            return module.getGlyphs().get(i);
        }
        return SpecialGlyphs.MISSING;
    }

    @Override
    public @NotNull BakedGlyph getGlyph(int i) {
        if (bakedGlyphs.containsKey(i)) {
            return bakedGlyphs.get(i);
        }
        if (module.getGlyphs().containsKey(i)) {
            // If we haven't baked a glyph yet we do so on the fly, vanilla
            // does the same in extreme cases.
            var cast = (FontSetExt) this;
            var info = module.getGlyphs().get(i);

            // If the image finished loading we bake and stitch it
            if (info.image.isDone()) {
                var baked = info.bake(cast::invokeStitch);
                bakedGlyphs.put(i, baked);
                return baked;
            } else {
                // If we already calculated the fallback we re-use it, we do this re-use per
                // properties object so we don't stitch copies of the same fallback image with
                // the same offset and advance.
                var properties = new GlyphProperties(info);
                if (fallbackBakedGlyphs.containsKey(properties)) {
                    return fallbackBakedGlyphs.get(properties);
                }

                // Create a new glyph that uses the fallback glyph image and bake/stitch it.
                var fallGlyph = new Glyph(properties.grayscale ? grayscaleFallbackGlyph : fallbackGlyph, properties);
                var baked = fallGlyph.bake(cast::invokeStitch);
                fallbackBakedGlyphs.put(properties, baked);
                return baked;
            }
        }

        // Try to figure out which claim this was and rebuild it if we somehow lost the glyph
        if (module.getClaims().inverse().containsKey((char) i)) {
            var properties = module.getClaims().inverse().get((char) i);
            var config = module.getLastConfig().get(properties);
            if (config != null) {
                module.loadGlyph(i, config);
            }
        }
        return super.getGlyph(i);
    }

    @Override
    public void close() {
        super.close();
        module.voidCaches();
    }

    public record GlyphProperties(boolean grayscale, float scale, int advance, int ascent) {

        public GlyphProperties(Glyph glyph) {
            this(glyph.grayscale(), glyph.scale(), glyph.advance(), glyph.ascent());
        }
    }

    @Environment(value = EnvType.CLIENT)
    public record Glyph(CompletableFuture<NativeImage> image, boolean grayscale, float scale, int advance, int ascent) implements GlyphInfo {

        public Glyph(CompletableFuture<NativeImage> image, SkullProperties properties) {
            this(image, properties.grayscale(), properties.scale(), properties.advance(), properties.ascent());
        }

        public Glyph(NativeImage image, GlyphProperties properties) {
            this(CompletableFuture.completedFuture(image), properties.grayscale(), properties.scale(), properties.advance(), properties.ascent());
        }

        @Override
        public float getAdvance() {
            // The skull has a width of 8, and we need to return size + 1
            return 9 + this.advance;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
            // We require the image to be present by baking time.
            var image = this.image.getNow(null);
            return function.apply(new SheetGlyphInfo() {

                @Override
                public float getOversample() {
                    return 1f / scale;
                }

                @Override
                public int getPixelWidth() {
                    return 8;
                }

                @Override
                public int getPixelHeight() {
                    return 8;
                }

                @Override
                public float getBearingY() {
                    return SheetGlyphInfo.super.getBearingY() + (float) ascent;
                }

                @Override
                public void upload(int i, int j) {
                    image.upload(0, i, j, 0, 0, 8, 8, false, false);
                }

                @Override
                public boolean isColored() {
                    return image.format().components() > 1;
                }
            });
        }
    }
}
