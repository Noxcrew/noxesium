package com.noxcrew.noxesium.api.network.json;

import com.google.gson.Gson;

/**
 * A JSON serializer backed by a GSON instance.
 */
public class GsonSerializer implements JsonSerializer {
    private final Gson gson;

    public GsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> String encode(T value, Class<T> clazz) {
        return gson.toJson(value);
    }

    @Override
    public <T> T decode(String string, Class<T> clazz) {
        return gson.fromJson(string, clazz);
    }
}
