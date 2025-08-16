package com.noxcrew.noxesium.core.util;

import java.util.UUID;

/**
 * Assists in formatting strings such that they are correct strings
 * to turn into skulls for Noxesium clients.
 */
public class SkullStringFormatter {

    /**
     * Writes the given skull info into a string.
     */
    public static String write(SkullInfo info) {
        var stringBuilder = new StringBuilder();
        if (info.raw()) {
            stringBuilder.append("%nox_raw%");
        } else {
            stringBuilder.append("%nox_uuid%");
        }
        stringBuilder.append(info.value).append(",");
        stringBuilder.append(info.grayscale).append(",");
        stringBuilder.append(info.advance).append(",");
        stringBuilder.append(info.ascent).append(",");
        stringBuilder.append(info.scale);
        return stringBuilder.toString();
    }

    /**
     * Parses the given input string into skull info.
     * @throws IllegalArgumentException If the string is invalid
     */
    public static SkullInfo parse(String input) throws IllegalArgumentException {
        boolean raw = false;
        String[] values;
        if (input.startsWith("%nox_uuid%")) {
            values = input.substring("%nox_uuid%".length()).split(",");
        } else if (input.startsWith("%nox_raw%")) {
            values = input.substring("%nox_raw%".length()).split(",");
            raw = true;
        } else {
            throw new IllegalArgumentException("Input string is not a valid skull info string");
        }

        var grayscale = Boolean.parseBoolean(values[1]);
        var advance = Integer.parseInt(values[2]);
        var ascent = Integer.parseInt(values[3]);
        var scale = Float.parseFloat(values[4]);
        return new SkullInfo(raw, values[0], grayscale, advance, ascent, scale);
    }

    /**
     * The data contained in a skull format string.
     *
     * @param raw If `true` the value is a raw texture, otherwise it's a uuid.
     * @param value The data of this skull.
     * @param grayscale Whether to draw the skull as grayscale.
     * @param advance The advance to give to the glyph.
     * @param ascent The ascent to give to the glyph.
     * @param scale The scale of the glyph.
     */
    public record SkullInfo(boolean raw, String value, boolean grayscale, int advance, int ascent, float scale) {

        public SkullInfo(String texture) {
            this(true, texture, false, 0, 0, 1f);
        }

        public SkullInfo(UUID uuid) {
            this(false, uuid.toString(), false, 0, 0, 1f);
        }
    }
}
