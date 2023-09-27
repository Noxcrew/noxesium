package com.noxcrew.noxesium.feature.skull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.platform.NativeImage;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.NoxesiumModule;
import com.noxcrew.noxesium.mixin.component.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Stores information about the currently known server rules and their data.
 */
public class SkullFontModule implements NoxesiumModule {

    public static ResourceLocation RESOURCE_LOCATION = new ResourceLocation(NoxesiumMod.NAMESPACE, "skulls");
    private static SkullFontModule instance;

    /**
     * Returns the singleton instance of the server rule module.
     */
    public static SkullFontModule getInstance() {
        if (instance == null) {
            instance = new SkullFontModule();
        }
        return instance;
    }

    private final BiMap<SkullProperties, Character> claims = HashBiMap.create();
    private final Map<SkullProperties, SkullConfig> lastConfig = new HashMap<>();
    private final Map<Integer, CustomSkullFont.Glyph> glyphs = new HashMap<>();
    private final Cache<Integer, Integer> grayscaleMappings = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();
    private int nextCharacter = 32;
    private UUID cache = UUID.randomUUID();

    private CustomSkullFont currentFont;

    @Override
    public void onStartup() {
        ClientTickEvents.END_CLIENT_TICK.register((ignored1) -> {
            // Create the custom skull font if it's not already created
            createIfNecessary();
        });
    }

    @Override
    public void onQuitServer() {
        // Clear out all font claims as we can now safely assume
        // we don't need the old ones anymore and there won't be
        // any components that persist between before/after this point
        clearCaches();
    }

    /**
     * Returns all claimed glyphs.
     */
    public Map<Integer, CustomSkullFont.Glyph> getGlyphs() {
        return glyphs;
    }

    /**
     * Returns all claims made on any character and by which skull.
     */
    public BiMap<SkullProperties, Character> getClaims() {
        return claims;
    }

    /**
     * Returns the last used configuration for each skull type.
     */
    public Map<SkullProperties, SkullConfig> getLastConfig() {
        return lastConfig;
    }

    /**
     * Returns a character in this font to store the texture of [texture].
     */
    public char claim(SkullConfig config) {
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
    public void loadGlyph(int next, SkullConfig config) {
        // Create the glyph for this skull
        var future = config.texture();
        if (future == null) return;

        // Await the completion of the future before baking the glyph
        var oldInstance = cache;

        // We immediately store the glyph data with a future promise to get the image
        var imageFuture = new CompletableFuture<NativeImage>();
        var properties = config.properties();
        var glyph = new CustomSkullFont.Glyph(imageFuture, properties);
        glyphs.put(next, glyph);

        future.whenComplete((texture, t) -> {
            // Stop waiting on old completables
            if (!Objects.equals(cache, oldInstance)) return;

            try {
                var gameProfile = new GameProfile(UUID.randomUUID(), "dummy_mcdummyface");
                gameProfile.getProperties().put(SkinManager.PROPERTY_TEXTURES, new Property(SkinManager.PROPERTY_TEXTURES, texture, ""));

                // Let the session servers extract the texture, don't check the signature
                var information = SkullBlockEntityExt.getSessionService().getTextures(gameProfile, false).get(MinecraftProfileTexture.Type.SKIN);
                if (information != null) {
                    String string = Hashing.sha1().hashUnencodedChars(information.getHash()).toString();

                    SkinManager.TextureCache skinTextures = ((SkinManagerExt) (Minecraft.getInstance().getSkinManager())).getSkinTextures();
                    Path rootPath = ((TextureCacheExt) skinTextures).getRootPath();
                    File file2 = rootPath.resolve(string.length() > 2 ? string.substring(0, 2) : "xx").resolve(string).toFile();

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
                        var httpTexture = new HttpTexture(file2, information.getUrl(), DefaultPlayerSkin.getDefaultTexture(), true, () -> {
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
     * Sets up a new custom skull font.
     */
    public void createIfNecessary() {
        if (currentFont != null) return;
        try {
            var instance = Minecraft.getInstance();
            var fontManager = ((MinecraftExt) instance).getFontManager();
            currentFont = new CustomSkullFont(this, instance.getTextureManager(), RESOURCE_LOCATION);

            // Reload to stitch in missing and blank glyphs
            currentFont.reload(List.of());

            // Manually input the font into the font manager's map of sets
            var current = ((FontManagerExt) fontManager).getFontSets();
            current.put(RESOURCE_LOCATION, currentFont);
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    /**
     * Voids all stored data, should only be called when the resource reload happens and
     * the current font is voided.
     */
    public void voidCaches() {
        // Whenever the font is closed we need to re-create the font next tick, ideally this would
        // be a mixin but the FontManager stores fonts in a local anonymous class which can't be
        // accessed through mixins. :/
        currentFont = null;
        resetCaches();
    }

    /**
     * Resets cached values on pack switch, persisting claims but invalidating cached sprites.
     */
    public void resetCaches() {
        glyphs.clear();
        cache = UUID.randomUUID();

        if (currentFont != null) {
            currentFont.voidBakedGlyphs();
        }
    }

    /**
     * Clear cached values on disconnection as it clears the chat history so we don't need the skulls anymore.
     */
    public void clearCaches() {
        resetCaches();
        claims.clear();
        lastConfig.clear();
        nextCharacter = 32;
    }

    /**
     * Converts the input RGB value into a grayscale RGG value.
     */
    public int toGrayscale(int input) {
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
    public NativeImage processImage(NativeImage input, boolean grayscale) {
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
}
