package com.noxcrew.noxesium.api.feature.qib;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.joml.Vector3f;

/**
 * Implements serialization for vectors.
 */
public class VectorSerializer implements JsonSerializer<Vector3f>, JsonDeserializer<Vector3f> {
    @Override
    public Vector3f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        var array = json.getAsJsonArray();
        return new Vector3f(
                array.get(0).getAsFloat(),
                array.get(1).getAsFloat(),
                array.get(2).getAsFloat());
    }

    @Override
    public JsonElement serialize(Vector3f src, Type typeOfSrc, JsonSerializationContext context) {
        var array = new JsonArray();
        array.add(src.x);
        array.add(src.y);
        array.add(src.z);
        return array;
    }
}
