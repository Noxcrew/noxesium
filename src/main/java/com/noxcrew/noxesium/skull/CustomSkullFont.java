package com.noxcrew.noxesium.skull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A custom font that contains glyphs for each skull that needs to be rendered.
 */
public class CustomSkullFont extends FontSet {

    public static ResourceLocation RESOURCE_LOCATION = new ResourceLocation("noxesium", "skulls");
    private static final Map<SkullConfig, Character> claims = new HashMap<>();
    private static final Map<Integer, GlyphInfo> glyphs = new HashMap<>();
    private static final Map<Integer, BakedGlyph> bakedGlyphs = new HashMap<>();
    private static final Cache<Integer, Integer> grayscaleMappings = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();
    private static int nextCharacter = 32;
    private static boolean created = false;
    private static UUID instance = UUID.randomUUID();

    public CustomSkullFont(TextureManager textureManager, ResourceLocation resourceLocation) {
        super(textureManager, resourceLocation);
    }

    /**
     * Sets up a new custom skull font.
     */
    public static void createIfNeccesary() {
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
            var baked = glyphs.get(i).bake(cast::invokeStitch);
            bakedGlyphs.put(i, baked);
            return baked;
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
    }

    /**
     * Clear cached values on disconnection as it clears the chat history so we don't need the skulls anymore.
     */
    public static void clear() {
        claims.clear();
        glyphs.clear();
        bakedGlyphs.clear();
        nextCharacter = 32;
        instance = UUID.randomUUID();
    }

    /**
     * Returns a character in this font to store the texture of [texture].
     */
    public static char claim(SkullConfig config) {
        // Use an existing claim if possible
        if (claims.containsKey(config)) {
            return claims.get(config);
        }

        // Otherwise make a new claim
        var next = nextCharacter++;
        claims.put(config, (char) next);

        // Create the glyph for this skull
        var future = config.texture();
        if (future == null) return (char) next;

        // Await the completion of the future before baking the glyph
        var oldInstance = instance;
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
                            buildGlyph(nativeImage, config, next);
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
                                buildGlyph(nativeImage, config, next);
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
        return (char) next;
    }

    /**
     * Converts the input RGB value into a grayscale RGG value.
     */
    private static int toGrayscale(int input) {
        try {
            return grayscaleMappings.get(input, () -> {
                var color = new Color(input);
                var val = (int) Math.round(0.2989 * color.getRed() + 0.5870 * color.getGreen() + 0.1140 * color.getBlue());
                return new Color(val, val, val, color.getAlpha()).getRGB();
            });
        } catch (Exception x) {
            x.printStackTrace();
            return input;
        }
    }

    /**
     * Builds the given image with the given config into a glyph.
     */
    private static void buildGlyph(NativeImage input, SkullConfig config, int next) {
        var target = new NativeImage(NativeImage.Format.RGBA, 8, 8, false);
        var grayscale = config.grayscale();

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

        glyphs.put(next, new Glyph(target, config.scale(), config.advance(), config.ascent()));
    }

    @Environment(value = EnvType.CLIENT)
    public record Glyph(NativeImage image, float scale, int advance, int ascent) implements GlyphInfo {
        @Override
        public float getAdvance() {
            // The skull has a width of 8, and we need to return size + 1
            return 9 + this.advance;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
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
                    return SheetGlyphInfo.super.getBearingY() - (float) ascent;
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
