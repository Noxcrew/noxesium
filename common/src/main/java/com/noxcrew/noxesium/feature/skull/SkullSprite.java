package com.noxcrew.noxesium.feature.skull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noxcrew.noxesium.api.protocol.skull.SkullStringFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.PlainTextRenderable;
import net.minecraft.client.gui.font.SingleSpriteSource;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * A custom chat component that renders a player's face at its location. The
 * input is directly received as a texture but can optionally be a unique id.
 */
public class SkullSprite implements ObjectInfo {
    /**
     * Caches all glyph sources to be used in rendering skull sprites.
     */
    public static final LoadingCache<SkullSprite, GlyphSource> GLYPH_SOURCE_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(PlayerSkinRenderCache.CACHE_DURATION)
            .build(new CacheLoader<>() {
                @Override
                public GlyphSource load(SkullSprite sprite) {
                    return new SingleSpriteSource(new BakedGlyph() {
                        @Override
                        public GlyphInfo info() {
                            return GlyphInfo.simple(8 * sprite.getScale() + sprite.getAdvance() + 1);
                        }

                        @Override
                        public TextRenderable createGlyph(
                                float x,
                                float y,
                                int color,
                                int shadowColor,
                                Style style,
                                float f,
                                float shadowOffset) {
                            return new SpriteInstance(sprite, x, y, color, shadowColor, shadowOffset);
                        }
                    });
                }
            });

    public static final MapCodec<SkullSprite> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                    UUIDUtil.STRING_CODEC
                            .optionalFieldOf("uuid")
                            .forGetter((skull) -> Optional.ofNullable(skull.getUuid())),
                    Codec.STRING
                            .optionalFieldOf("texture")
                            .forGetter((skull) -> Optional.ofNullable(skull.getTexture())),
                    Codec.BOOL.optionalFieldOf("grayscale", false).forGetter(SkullSprite::isGrayscale),
                    Codec.INT.optionalFieldOf("advance", 0).forGetter(SkullSprite::getAdvance),
                    Codec.INT.optionalFieldOf("ascent", 0).forGetter(SkullSprite::getAscent),
                    Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(SkullSprite::getScale),
                    Codec.BOOL.optionalFieldOf("hat", true).forGetter(SkullSprite::hasHat))
            .apply(instance, SkullSprite::new));

    private static final String PROPERTY_TEXTURES = "textures";

    /**
     * The signature used when checking the skin. We do not care if this signature is correct, but
     * the signature needs to be of a valid length to not throw an error.
     */
    private static final String RANDOM_SIGNATURE =
            "Lcgr04dLPH0GHOPFdI2/JdFM3wpXEEt2PGh0uc8P7AcUb+PLOpyazC7VWhtT2H2TyKA5qK6Qeg04pJ3dnFWW+ToRnnVkLxhk1pv7tZEVIj98d1eRy6BxQ4A6eihplyquSAjrb1xMii9W5PM0HcwHiai5yo/1keey9Sq4Nk3bI3DWzJjNGEEACAhsCdezYTzwPsIa8xqnXPi0r2vVQe0nLkgDInDWslyp+UbzKxmMx5IK920iEZhrHhDkmj9yC1Sn7L7lPW0kz7iRlXsnpVJ36JSCma/i57dOWDJbEWpZTnH8TqsyHLPY+voFU+D1UzUkgvOWXL3YAJfajhBZsk0NhFyio9iRh8delBksYdd87q7eu9q35gwUMiooaMxkJupz9tuS1MKMtalYTWXak3pxROMIBiS6kp85fpSd1a18JN6WivvjdDGjC6azL8zf2/ie2GFhSeo+a2HkaXqcuuYcWUTo2CDmTsgCYiTC0GpHA0rClFfpLaVVCZU9TPG4ErUy1HOXhc9R5+CRd4qQG+1LGbfddxsnNpp5Vv8DGS6roQw7zW4DwL7AOQZuw5QrEc6cqqEp/7/gejRSiYj2CXHw4wlVfhPqG+7w7waLHfq/5ZTCVXNLW/kCOD18vVFsNIc6oZjNgtDuwRrUjMX8LIFL2ERKx76FPlzUV40GQ4ZjJeE=";

    @Nullable
    private final UUID uuid;

    @Nullable
    private final String texture;

    private final Supplier<PlayerSkinRenderCache.RenderInfo> skin;
    private final boolean grayscale;
    private final int advance;
    private final int ascent;
    private final float scale;
    private final boolean hat;

    public SkullSprite(
            Optional<UUID> uuid,
            Optional<String> texture,
            boolean grayscale,
            int advance,
            int ascent,
            float scale,
            boolean hat) {
        ResolvableProfile profile = null;
        if (texture.isPresent()) {
            var gameProfile = new GameProfile(UUID.randomUUID(), "");
            gameProfile
                    .properties()
                    .put(PROPERTY_TEXTURES, new Property(PROPERTY_TEXTURES, texture.get(), RANDOM_SIGNATURE));
            profile = ResolvableProfile.createResolved(gameProfile);
        } else if (uuid.isPresent()) {
            profile = ResolvableProfile.createUnresolved(uuid.get());
        }
        if (profile != null) {
            this.skin = Minecraft.getInstance().playerSkinRenderCache().createLookup(profile);
        } else {
            var gameProfile = new GameProfile(UUID.randomUUID(), "");
            var defaultSkin = DefaultPlayerSkin.getDefaultSkin();
            var renderInfo = Minecraft.getInstance().playerSkinRenderCache()
            .new RenderInfo(gameProfile, defaultSkin, PlayerSkin.Patch.EMPTY);
            this.skin = () -> renderInfo;
        }

        this.uuid = uuid.orElse(null);
        this.texture = texture.orElse(null);
        this.grayscale = grayscale;
        this.advance = advance;
        this.ascent = ascent;
        this.scale = scale;
        this.hat = hat;
    }

    @Nullable
    public UUID getUuid() {
        return uuid;
    }

    @Nullable
    public String getTexture() {
        return texture;
    }

    public boolean isGrayscale() {
        return grayscale;
    }

    public int getAdvance() {
        return advance;
    }

    public int getAscent() {
        return ascent;
    }

    public float getScale() {
        return scale;
    }

    public boolean hasHat() {
        return hat;
    }

    @Override
    public String toString() {
        return "skull{texture='" + texture + "', grayscale='" + grayscale + "', advance='" + advance + "', ascent='"
                + ascent + "', scale='" + scale + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SkullSprite that)) return false;
        if (!super.equals(o)) return false;
        return grayscale == that.grayscale
                && advance == that.advance
                && ascent == that.ascent
                && Float.compare(that.scale, scale) == 0
                && Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid, grayscale, advance, ascent, scale);
    }

    @Override
    public FontDescription fontDescription() {
        return new SkullFontDescription(this);
    }

    @Override
    public String description() {
        // Never indicate whose player head it is because we want to support disguising!
        return "[player head]";
    }

    @Override
    public MapCodec<SkullSprite> codec() {
        return MAP_CODEC;
    }

    /**
     * Creates a new skull component out of the given info.
     */
    public static Component create(SkullStringFormatter.SkullInfo info) {
        if (info.raw()) {
            return create(info.value(), info.grayscale(), info.advance(), info.ascent(), info.scale());
        } else {
            return create(UUID.fromString(info.value()), info.grayscale(), info.advance(), info.ascent(), info.scale());
        }
    }

    /**
     * Creates a new skull component for the player with the given uuid.
     */
    public static Component create(UUID uuid, boolean grayscale, int advance, int ascent, float scale) {
        return create(uuid, grayscale, advance, ascent, scale, true);
    }

    /**
     * Creates a new skull component for the player with the given uuid.
     */
    public static Component create(UUID uuid, boolean grayscale, int advance, int ascent, float scale, boolean hat) {
        return MutableComponent.create(new ObjectContents(
                new SkullSprite(Optional.of(uuid), Optional.empty(), grayscale, advance, ascent, scale, hat)));
    }

    /**
     * Creates a new skull component using the given texture.
     */
    public static Component create(String texture, boolean grayscale, int advance, int ascent, float scale) {
        return create(texture, grayscale, advance, ascent, scale, true);
    }

    /**
     * Creates a new skull component using the given texture.
     */
    public static Component create(
            String texture, boolean grayscale, int advance, int ascent, float scale, boolean hat) {
        return MutableComponent.create(new ObjectContents(
                new SkullSprite(Optional.empty(), Optional.of(texture), grayscale, advance, ascent, scale, hat)));
    }

    private record SpriteInstance(SkullSprite sprite, float x, float y, int color, int shadowColor, float shadowOffset)
            implements PlainTextRenderable {
        @Override
        public void renderSprite(
                Matrix4f matrix,
                VertexConsumer vertexConsumer,
                int p_443287_,
                float p_443341_,
                float p_443360_,
                float p_443552_,
                int color) {
            float f = p_443341_ + this.left();
            float f1 = p_443341_ + this.right();
            float f2 = p_443360_ + this.top();
            float f3 = p_443360_ + this.bottom();
            renderQuad(matrix, vertexConsumer, p_443287_, f, f1, f2, f3, p_443552_, color, 8.0F, 8.0F, 8, 8, 64, 64);
            if (sprite.hasHat()) {
                renderQuad(
                        matrix, vertexConsumer, p_443287_, f, f1, f2, f3, p_443552_, color, 40.0F, 8.0F, 8, 8, 64, 64);
            }
        }

        private static void renderQuad(
                Matrix4f matrix,
                VertexConsumer vertexConsumer,
                int light,
                float x0,
                float x1,
                float y,
                float z0,
                float z1,
                int color,
                float u,
                float v,
                int width,
                int height,
                int sizeX,
                int sizeZ) {
            float u0 = (u + 0.0F) / sizeX;
            float u1 = (u + width) / sizeX;
            float v0 = (v + 0.0F) / sizeZ;
            float v1 = (v + height) / sizeZ;
            vertexConsumer
                    .addVertex(matrix, x0, y, z1)
                    .setUv(u0, v0)
                    .setColor(color)
                    .setLight(light);
            vertexConsumer
                    .addVertex(matrix, x0, z0, z1)
                    .setUv(u0, v1)
                    .setColor(color)
                    .setLight(light);
            vertexConsumer
                    .addVertex(matrix, x1, z0, z1)
                    .setUv(u1, v1)
                    .setColor(color)
                    .setLight(light);
            vertexConsumer
                    .addVertex(matrix, x1, y, z1)
                    .setUv(u1, v0)
                    .setColor(color)
                    .setLight(light);
        }

        @Override
        public RenderType renderType(Font.DisplayMode p_442599_) {
            return sprite.skin.get().glyphRenderTypes().select(p_442599_);
        }

        @Override
        public RenderPipeline guiPipeline() {
            return sprite.skin.get().glyphRenderTypes().guiPipeline();
        }

        @Override
        public GpuTextureView textureView() {
            return sprite.skin.get().textureView();
        }

        @Override
        public float left() {
            return 0.0F;
        }

        @Override
        public float right() {
            return left() + 8.0F * sprite.getScale();
        }

        @Override
        public float top() {
            return (float) sprite.getAscent();
        }

        @Override
        public float bottom() {
            return top() + 8.0F * sprite.getScale();
        }
    }
}
