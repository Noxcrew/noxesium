package com.noxcrew.noxesium.skull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.noxcrew.noxesium.mixin.client.component.FontManagerExt;
import com.noxcrew.noxesium.mixin.client.component.FontSetExt;
import com.noxcrew.noxesium.mixin.client.component.MinecraftExt;
import com.noxcrew.noxesium.mixin.client.component.SkinManagerExt;
import com.noxcrew.noxesium.mixin.client.component.SkullBlockEntityExt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A custom font that contains glyphs for each skull that needs to be rendered.
 */
public class CustomSkullFont extends FontSet {

    public static ResourceLocation RESOURCE_LOCATION = new ResourceLocation("noxesium", "skulls");

    private static final BiMap<SkullProperties, Character> claims = HashBiMap.create();
    private static final Map<SkullProperties, SkullConfig> lastConfig = new HashMap<>();
    private static final Map<Integer, Glyph> glyphs = new HashMap<>();
    private static final Map<Integer, BakedGlyph> bakedGlyphs = new HashMap<>();
    private static final Map<GlyphProperties, BakedGlyph> fallbackBakedGlyphs = new HashMap<>();
    private static final Cache<Integer, Integer> grayscaleMappings = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();
    private static int nextCharacter = 32;
    private static boolean created = false;
    private static UUID instance = UUID.randomUUID();

    private NativeImage fallbackGlyph;
    private NativeImage grayscaleFallbackGlyph;

    public CustomSkullFont(TextureManager textureManager, ResourceLocation resourceLocation) {
        super(textureManager, resourceLocation);

        try {
            ResourceLocation location = DefaultPlayerSkin.getDefaultSkin();
            var simpleTexture = SimpleTexture.TextureImage.load(Minecraft.getInstance().getResourceManager(), location);
            var image = simpleTexture.getImage();

            fallbackGlyph = processImage(image, false);
            grayscaleFallbackGlyph = processImage(image, true);
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    /**
     * Sets up a new custom skull font.
     */
    public static void createIfNecessary() {
        if (created) return;
        created = true;

        try {
            var instance = Minecraft.getInstance();
            var fontManager = ((MinecraftExt) instance).getFontManager();
            FontSet fontSet = new CustomSkullFont(instance.getTextureManager(), RESOURCE_LOCATION);

            // Reload to stitch in missing and blank glyphs
            fontSet.reload(List.of());

            // Manually input the font into the font manager's map of sets
            var current = ((FontManagerExt) fontManager).getFontSets();
            current.put(RESOURCE_LOCATION, fontSet);
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    @Override
    public GlyphInfo getGlyphInfo(int i, boolean bl) {
        if (glyphs.containsKey(i)) {
            return glyphs.get(i);
        }
        return SpecialGlyphs.MISSING;
    }

    @Override
    public BakedGlyph getGlyph(int i) {
        if (bakedGlyphs.containsKey(i)) {
            return bakedGlyphs.get(i);
        }
        if (glyphs.containsKey(i)) {
            // If we haven't baked a glyph yet we do so on the fly, vanilla
            // does the same in extreme cases.
            var cast = (FontSetExt) this;
            var info = glyphs.get(i);

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

        // Try to figure out which claim this was and rebuild it
        if (claims.inverse().containsKey((char) i)) {
            var properties = claims.inverse().get((char) i);
            var config = lastConfig.get(properties);
            if (config != null) {
                loadGlyph(i, config);
            }
        }
        return super.getGlyph(i);
    }

    @Override
    public void close() {
        super.close();

        // Whenever the font is closed we need to re-create the font next tick, ideally this would
        // be a mixin but the FontManager stores fonts in a local anonymous class which can't be
        // accessed through mixins. :/
        created = false;
        resetCaches();
    }

    /**
     * Resets cached values on pack switch, persisting claims but invalidating cached sprites.
     */
    public static void resetCaches() {
        glyphs.clear();
        bakedGlyphs.clear();
        instance = UUID.randomUUID();
    }

    /**
     * Clear cached values on disconnection as it clears the chat history so we don't need the skulls anymore.
     */
    public static void clearCaches() {
        resetCaches();
        claims.clear();
        lastConfig.clear();
        nextCharacter = 32;
    }

    /**
     * Returns a character in this font to store the texture of [texture].
     */
    public static char claim(SkullConfig config) {
        // Use an existing claim if possible
        var properties = config.properties();
        lastConfig.put(properties, config);
        if (claims.containsKey(properties)) {
            return claims.get(properties);
        }

        // Otherwise make a new claim
        var next = nextCharacter++;
        claims.put(properties, (char) next);

        // Start loading this glyph
        loadGlyph(next, config);

        return (char) next;
    }

    /**
     * Loads in a glyph into character [next] based on config [config].
     */
    private static void loadGlyph(int next, SkullConfig config) {
        // Create the glyph for this skull
        var future = config.texture();
        if (future == null) return;

        // Await the completion of the future before baking the glyph
        var oldInstance = instance;

        // We immediately store the glyph data with a future promise to get the image
        var imageFuture = new CompletableFuture<NativeImage>();
        var properties = config.properties();
        var glyph = new Glyph(imageFuture, properties);
        glyphs.put(next, glyph);

        future.whenComplete((texture, t) -> {
            // Stop waiting on old completables
            if (!Objects.equals(instance, oldInstance)) return;

            try {
                var gameProfile = new GameProfile(null, "dummy_mcdummyface");
                gameProfile.getProperties().put(SkinManager.PROPERTY_TEXTURES, new Property(SkinManager.PROPERTY_TEXTURES, texture, ""));

                // Let the session servers extract the texture, don't check the signature
                var information = SkullBlockEntityExt.getSessionService().getTextures(gameProfile, false).get(MinecraftProfileTexture.Type.SKIN);
                if (information != null) {
                    String string = Hashing.sha1().hashUnencodedChars(information.getHash()).toString();
                    File file = new File(((SkinManagerExt) (Minecraft.getInstance().getSkinManager())).getSkinsDirectory(), string.length() > 2 ? string.substring(0, 2) : "xx");
                    File file2 = new File(file, string);

                    if (file2.exists()) {
                        // If the skin already exists we load it in
                        try {
                            NativeImage nativeImage;
                            try (InputStream inputStream = new FileInputStream(file2)) {
                                nativeImage = NativeImage.read(inputStream);
                            }
                            imageFuture.complete(processImage(nativeImage, properties.grayscale()));
                        } catch (IOException x) {
                            x.printStackTrace();
                        }
                    } else {
                        // If this skin isn't known we download it first
                        var resourceLocation = new ResourceLocation("skins/" + string);
                        var httpTexture = new HttpTexture(file2, information.getUrl(), DefaultPlayerSkin.getDefaultSkin(), true, () -> {
                            // At this point the texture has been saved to the file, so we can read out the native image
                            try {
                                NativeImage nativeImage;
                                try (InputStream inputStream = new FileInputStream(file2)) {
                                    nativeImage = NativeImage.read(inputStream);
                                }
                                imageFuture.complete(processImage(nativeImage, properties.grayscale()));
                            } catch (IOException x) {
                                x.printStackTrace();
                            }
                        });

                        // Let the texture manager register the skin and do the downloading for us
                        Minecraft.getInstance().getTextureManager().register(resourceLocation, httpTexture);
                    }
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        });
    }

    /**
     * Converts the input RGB value into a grayscale RGG value.
     */
    private static int toGrayscale(int input) {
        try {
            return grayscaleMappings.get(input, () -> {
                var color = new Color(input, true);
                var val = (int) Math.round(0.2989 * color.getRed() + 0.5870 * color.getGreen() + 0.1140 * color.getBlue());
                return new Color(val, val, val, color.getAlpha()).getRGB();
            });
        } catch (Exception x) {
            x.printStackTrace();
            return input;
        }
    }

    /**
     * Processes the given input image.
     */
    private static NativeImage processImage(NativeImage input, boolean grayscale) {
        var target = new NativeImage(NativeImage.Format.RGBA, 8, 8, false);

        // Copy over the base layer
        for (int x = 8; x < 16; x++) {
            for (int y = 8; y < 16; y++) {
                var pixel = input.getPixelRGBA(x, y);
                target.setPixelRGBA(x - 8, y - 8, grayscale ? toGrayscale(pixel) : pixel);
            }
        }

        // Copy the hat layer into the image using the blend function
        for (int x = 40; x < 48; x++) {
            for (int y = 8; y < 16; y++) {
                var pixel = input.getPixelRGBA(x, y);
                if (pixel != 0) {
                    target.blendPixel(x - 40, y - 8, grayscale ? toGrayscale(pixel) : pixel);
                }
            }
        }
        return target;
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
