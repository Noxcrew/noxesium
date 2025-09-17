package com.noxcrew.noxesium.api.network.json;

/**
 * Provides a simplistic view of a JSON serializer.
 */
public interface JsonSerializer {
    /**
     * Encodes the given value into a JSON string.
     *
     * @param value The input value to encode.
     * @param clazz The class of object T.
     * @return A JSON-encoded string.
     */
    <T> String encode(T value, Class<T> clazz);

    /**
     * Decodes the given string into a value of type T.
     *
     * @param string The input JSON-encoded string.
     * @param clazz The class of object T.
     * @return An object of type T.
     */
    <T> T decode(String string, Class<T> clazz);
}
