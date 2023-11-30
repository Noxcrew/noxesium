package com.noxcrew.noxesium.feature.skull;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A custom chat component that renders a player's face at its location. The
 * input is directly received as a texture but can optionally be a unique id.
 * <p>
 * We hide the skull content as a type of TranslatableContents so we can convert
 * newly created TranslatableContents into a skull content if we detect special
 * formatting to allow servers to hide skulls in translation contents.
 */
public class SkullContents extends TranslatableContents {
    public static final MapCodec<SkullContents> INNER_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance
            .group(
                    UUIDUtil.CODEC.optionalFieldOf("uuid", null).forGetter(SkullContents::getUuid),
                    Codec.STRING.optionalFieldOf("texture", null).forGetter(SkullContents::getTexture),
                    Codec.BOOL.optionalFieldOf("grayscale", false).forGetter(SkullContents::isGrayscale),
                    Codec.INT.optionalFieldOf("advance", 0).forGetter(SkullContents::getAdvance),
                    Codec.INT.optionalFieldOf("ascent", 0).forGetter(SkullContents::getAscent),
                    Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(SkullContents::getScale)
            )
            .apply(instance, SkullContents::new));
    public static final MapCodec<SkullContents> CODEC = INNER_CODEC.fieldOf("skull");
    public static final ComponentContents.Type<SkullContents> TYPE = new ComponentContents.Type<>(CODEC, "skull");

    @Nullable
    private final UUID uuid;
    private final CompletableFuture<String> texture;
    private final boolean grayscale;
    private final int advance;
    private final int ascent;
    private final float scale;
    private final SkullConfig config;

    public SkullContents(@Nullable UUID uuid, CompletableFuture<String> texture, boolean grayscale, int advance, int ascent, float scale) {
        super("", null, new Object[]{});
        this.uuid = uuid;
        this.texture = texture;
        this.grayscale = grayscale;
        this.advance = advance;
        this.ascent = ascent;
        this.scale = scale;
        this.config = new SkullConfig(texture, new SkullProperties(this));
    }

    public SkullContents(@Nullable UUID uuid, @Nullable String textureIn, boolean grayscale, int advance, int ascent, float scale) {
        super("", null, new Object[]{});
        CompletableFuture<String> texture = new CompletableFuture<>();
        if (textureIn != null) {
            texture.complete(textureIn);
        } else if (uuid != null) {
            try {
                GameProfile gameprofile = new GameProfile(uuid, "dummy_mcdummyface");
                GameProfileFetcher.updateGameProfile(gameprofile, (profile) -> {
                    var property = Iterables.getFirst(profile.getProperties().get(GameProfileFetcher.PROPERTY_TEXTURES), null);
                    if (property != null) {
                        texture.complete(property.value());
                    }
                });
            } catch (Exception x) {
                // We ignore any errors from fetching the player data.
            }
        }

        this.uuid = uuid;
        this.texture = texture;
        this.grayscale = grayscale;
        this.advance = advance;
        this.ascent = ascent;
        this.scale = scale;
        this.config = new SkullConfig(texture, new SkullProperties(this));
    }

    @Override
    public ComponentContents.Type<?> type() {
        return TYPE;
    }

    @Nullable
    public UUID getUuid() {
        return uuid;
    }

    @Nullable
    public String getTexture() {
        // We simply use whatever texture we currently have, otherwise we lose the data.
        return texture.getNow(null);
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

    /**
     * Returns the plain-text representation of this skull in the skull font.
     */
    public String getText() {
        return Character.toString(SkullFontModule.getInstance().claim(config));
    }

    public SkullConfig getConfig() {
        return config;
    }

    public SkullProperties getProperties() {
        return config.properties();
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        return contentConsumer.accept(getText());
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        return styledContentConsumer.accept(style.withFont(SkullFontModule.RESOURCE_LOCATION), getText());
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) {
        return Component.literal(getText()).setStyle(Style.EMPTY.withFont(SkullFontModule.RESOURCE_LOCATION));
    }

    @Override
    public String toString() {
        return "skull{texture='" + texture + "', grayscale='" + grayscale + "', advance='" + advance + "', ascent='" + ascent + "', scale='" + scale + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SkullContents that)) return false;
        if (!super.equals(o)) return false;
        return grayscale == that.grayscale && advance == that.advance && ascent == that.ascent && Float.compare(that.scale, scale) == 0 && Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid, grayscale, advance, ascent, scale);
    }
}
