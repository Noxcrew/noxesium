package com.noxcrew.noxesium.api.feature.sprite;

import java.util.Map;

/**
 * The basis for a custom sprite type that can be encoded into a string.
 */
public interface CustomSpriteType {
    /**
     * Returns the type id of this sprite.
     */
    String type();

    /**
     * Returns the properties of this sprite type as a map.
     */
    Map<String, String> properties();

    /**
     * Serialize this custom sprite type to a string.
     */
    default String serialize() {
        var stringBuilder = new StringBuilder();
        stringBuilder.append("%nox:");
        stringBuilder.append(type());
        stringBuilder.append("[");
        var first = true;
        for (var entry : properties().entrySet()) {
            if (!first) {
                stringBuilder.append(",");
            }
            stringBuilder.append(entry.getKey());
            stringBuilder.append("=");
            stringBuilder.append(entry.getValue());
            first = false;
        }
        stringBuilder.append("]%");
        return stringBuilder.toString();
    }
}
