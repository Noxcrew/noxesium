package com.noxcrew.noxesium.api.feature.sprite;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

/**
 * Handles the registry for custom sprite types that can be deserialized.
 * <p>
 * Assists in formatting custom sprite types within a translation
 * component that are shown only for Noxesium clients.
 * <p>
 * All custom sprites are all formatted as follows within translation
 * components:
 * %nox:type[key=value,foo=bar]%
 */
public class CustomSpriteRegistry {
    private static final Map<String, Function<Map<String, String>, CustomSpriteType>> customSpriteTypes =
            new HashMap<>();

    /**
     * Registers a new type of custom sprite using the given parser.
     */
    public static void register(String type, Function<Map<String, String>, CustomSpriteType> parser) {
        if (customSpriteTypes.containsKey(type))
            throw new IllegalArgumentException("Cannot register type '" + type + "' multiple times");
        customSpriteTypes.put(type, parser);
    }

    /**
     * Creates a new custom sprite of the given type with the given properties.
     * Returns `null` if the type is not recognized.
     */
    @Nullable
    public static CustomSpriteType create(String type, Map<String, String> properties) {
        var parser = customSpriteTypes.get(type);
        if (parser == null) return null;
        return parser.apply(properties);
    }

    /**
     * Attempts to parse the given input as a Noxesium custom sprite type.
     */
    public static CustomSpriteType parse(String input) {
        // Ignore if this is not a valid custom Noxesium translation component
        if (!input.startsWith("%nox")) return null;

        // Try to parse a legacy skull
        var legacy = parseLegacySkulls(input);
        if (legacy != null) return legacy;

        // Ignore if this is not a valid custom sprite
        if (!input.startsWith("%nox:")) return null;

        // Read out the string as we go
        var head = 5;
        var size = input.length();
        StringBuilder str = new StringBuilder();
        String type = null;
        String key = null;
        var properties = new HashMap<String, String>();
        while (head < size) {
            var character = input.charAt(head);
            if (character == '%' || character == ']') {
                // End of syntax, stop looking!
                break;
            } else if (character == '[') {
                // End of type, finish it!
                type = str.toString();
                str = new StringBuilder();
            } else if (character == ',') {
                // End of a block, store it if we have a valid key!
                if (key != null) {
                    properties.put(key, str.toString());
                    key = null;
                }
                str = new StringBuilder();
            } else if (character == '=') {
                // End of a key, store it!
                key = str.toString();
                str = new StringBuilder();
            } else {
                str.append(character);
            }
            head++;
        }
        if (key != null) {
            properties.put(key, str.toString());
        }
        if (type != null) {
            return create(type, properties);
        }
        return null;
    }

    /**
     * Parses the given input string for a pre-V3 skull info.
     */
    @Nullable
    public static CustomSkullSprite parseLegacySkulls(String input) {
        boolean raw = false;
        String[] values;
        if (input.startsWith("%nox_uuid%")) {
            values = input.substring("%nox_uuid%".length()).split(",");
        } else if (input.startsWith("%nox_raw%")) {
            values = input.substring("%nox_raw%".length()).split(",");
            raw = true;
        } else {
            return null;
        }
        var advance = Integer.parseInt(values[2]);
        var ascent = Integer.parseInt(values[3]);
        var scale = Float.parseFloat(values[4]);
        var hat = true;
        if (values.length >= 6) {
            hat = Boolean.parseBoolean(values[5]);
        }
        return new CustomSkullSprite(raw, values[0], advance, ascent, scale, hat);
    }
}
