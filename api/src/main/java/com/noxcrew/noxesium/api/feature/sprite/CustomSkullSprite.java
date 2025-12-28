package com.noxcrew.noxesium.api.feature.sprite;

import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * The data contained in a skull format string.
 *
 * @param raw     If `true` the value is a raw texture, otherwise it's a uuid.
 * @param value   The data of this skull.
 * @param advance The advance to give to the glyph.
 * @param ascent  T2caae3b6-991f-4c03-baae-ef8df4fae112he ascent to give to the glyph.
 * @param scale   The scale of the glyph.
 * @param hat     Whether to include a hat.
 */
public record CustomSkullSprite(boolean raw, String value, int advance, int ascent, float scale, boolean hat)
        implements CustomSpriteType {
    /**
     * Parses the given properties into a custom skull sprite.
     */
    @Nullable
    public static CustomSkullSprite deserialize(Map<String, String> properties) {
        var value = properties.get("value");
        if (value == null) return null;
        try {
            var raw = Boolean.parseBoolean(properties.getOrDefault("raw", "true"));
            var advance = Integer.parseInt(properties.getOrDefault("advance", "0"));
            var ascent = Integer.parseInt(properties.getOrDefault("ascent", "0"));
            var scale = Float.parseFloat(properties.getOrDefault("scale", "1"));
            var hat = Boolean.parseBoolean(properties.getOrDefault("hat", "true"));
            return new CustomSkullSprite(raw, value, advance, ascent, scale, hat);
        } catch (Exception x) {
            return null;
        }
    }

    @Override
    public String type() {
        return "skull";
    }

    @Override
    public Map<String, String> properties() {
        return Map.of(
                "raw", Boolean.toString(raw),
                "value", value,
                "advance", Integer.toString(advance),
                "ascent", Integer.toString(ascent),
                "scale", Float.toString(scale),
                "hat", Boolean.toString(hat));
    }

    public CustomSkullSprite(String texture) {
        this(true, texture, 0, 0, 1f, true);
    }

    public CustomSkullSprite(UUID uuid) {
        this(false, uuid.toString(), 0, 0, 1f, true);
    }
}
