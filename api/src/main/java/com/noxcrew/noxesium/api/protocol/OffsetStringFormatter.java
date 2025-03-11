package com.noxcrew.noxesium.api.protocol;

/**
 * The `insertion` string of any component Style object can be modified
 * to add any (X, Y) offset to the rendered location of a component.
 * <p>
 * This class assists in creating the format.
 */
public class OffsetStringFormatter {

    private static final int PREFIX_LENGTH = "%nox_offset%".length();

    /**
     * Serializes the given offset into a string.
     */
    public static String write(ComponentOffset offset) {
        return "%nox_offset%" + offset.x + "," + offset.y;
    }

    /**
     * Parses the given input string and returns its X value.
     */
    public static Float parseX(String input) {
        if (input != null && input.startsWith("%nox_offset%")) {
            var base = input.substring(PREFIX_LENGTH);
            var commaIndex = base.indexOf(",");
            if (commaIndex != -1) {
                var number = base.substring(0, commaIndex);
                try {
                    return Float.parseFloat(number);
                } catch (NumberFormatException x) {
                }
            }
        }
        return null;
    }

    /**
     * Parses the given input string and returns its Y value.
     */
    public static Float parseY(String input) {
        if (input != null && input.startsWith("%nox_offset%")) {
            var base = input.substring(PREFIX_LENGTH);
            var commaIndex = base.indexOf(",");
            if (commaIndex != -1) {
                var number = base.substring(commaIndex + 1);
                try {
                    return Float.parseFloat(number);
                } catch (NumberFormatException x) {
                }
            }
        }
        return null;
    }

    /**
     * Stores the offset to give to a component.
     */
    public record ComponentOffset(float x, float y) {
    }
}
