package com.noxcrew.noxesium.feature.skull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.platform.NativeImage;
import com.noxcrew.noxesium.NoxesiumModule;
import com.noxcrew.noxesium.api.protocol.ProtocolVersion;
import com.noxcrew.noxesium.feature.ui.wrapper.ElementManager;
import com.noxcrew.noxesium.feature.ui.wrapper.ElementWrapper;
import com.noxcrew.noxesium.mixin.component.ext.FontManagerExt;
import com.noxcrew.noxesium.mixin.component.ext.MinecraftExt;
import com.noxcrew.noxesium.mixin.component.ext.SkinManagerExt;
import com.noxcrew.noxesium.mixin.component.ext.TextureCacheExt;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.Util;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Stores information about the currently known server rules and their data.
 */
public class SkullFontModule implements NoxesiumModule {

    public static ResourceLocation RESOURCE_LOCATION = ResourceLocation.fromNamespaceAndPath(ProtocolVersion.NAMESPACE, "skulls");

    /**
     * The signature used when checking the skin. We do not care if this signature is correct, but
     * the signature needs to be of a valid length to not throw an error.
     */
    private static final String RANDOM_SIGNATURE = "Lcgr04dLPH0GHOPFdI2/JdFM3wpXEEt2PGh0uc8P7AcUb+PLOpyazC7VWhtT2H2TyKA5qK6Qeg04pJ3dnFWW+ToRnnVkLxhk1pv7tZEVIj98d1eRy6BxQ4A6eihplyquSAjrb1xMii9W5PM0HcwHiai5yo/1keey9Sq4Nk3bI3DWzJjNGEEACAhsCdezYTzwPsIa8xqnXPi0r2vVQe0nLkgDInDWslyp+UbzKxmMx5IK920iEZhrHhDkmj9yC1Sn7L7lPW0kz7iRlXsnpVJ36JSCma/i57dOWDJbEWpZTnH8TqsyHLPY+voFU+D1UzUkgvOWXL3YAJfajhBZsk0NhFyio9iRh8delBksYdd87q7eu9q35gwUMiooaMxkJupz9tuS1MKMtalYTWXak3pxROMIBiS6kp85fpSd1a18JN6WivvjdDGjC6azL8zf2/ie2GFhSeo+a2HkaXqcuuYcWUTo2CDmTsgCYiTC0GpHA0rClFfpLaVVCZU9TPG4ErUy1HOXhc9R5+CRd4qQG+1LGbfddxsnNpp5Vv8DGS6roQw7zW4DwL7AOQZuw5QrEc6cqqEp/7/gejRSiYj2CXHw4wlVfhPqG+7w7waLHfq/5ZTCVXNLW/kCOD18vVFsNIc6oZjNgtDuwRrUjMX8LIFL2ERKx76FPlzUV40GQ4ZjJeE=";

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
                var gameProfile = new GameProfile(Util.NIL_UUID, "dummy_mcdummyface");
                gameProfile.getProperties().put(GameProfileFetcher.PROPERTY_TEXTURES, new Property(GameProfileFetcher.PROPERTY_TEXTURES, texture, RANDOM_SIGNATURE));

                // Let the session servers extract the texture, don't check the signature
                var information = Minecraft.getInstance().getMinecraftSessionService().getTextures(gameProfile).skin();
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
                            ElementManager.getAllWrappers().forEach(ElementWrapper::requestRedraw);
                        } catch (IOException x) {
                            x.printStackTrace();
                        }
                    } else {
                        // If this skin isn't known we download it first
                        var resourceLocation = ResourceLocation.withDefaultNamespace("skins/" + string);
                        var httpTexture = new HttpTexture(file2, information.getUrl(), DefaultPlayerSkin.getDefaultTexture(), true, () -> {
                            // At this point the texture has been saved to the file, so we can read out the native image
                            try {
                                NativeImage nativeImage;
                                try (InputStream inputStream = new FileInputStream(file2)) {
                                    nativeImage = NativeImage.read(inputStream);
                                }
                                imageFuture.complete(processImage(nativeImage, properties.grayscale()));
                                ElementManager.getAllWrappers().forEach(ElementWrapper::requestRedraw);
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
            currentFont.reload(Set.of());

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
