package com.noxcrew.noxesium.api.network.json;

import com.google.common.base.Preconditions;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds different JSON serialization implementations which can be used for serialization of packets.
 */
public class JsonSerializerRegistry {
    private static final JsonSerializerRegistry INSTANCE = new JsonSerializerRegistry();

    /**
     * Returns an instance of the JSON serializer registry.
     */
    public static JsonSerializerRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * The identifier of the default serializer which is always available.
     */
    public static final String DEFAULT_SERIALIZER = "gson-default";

    private final Map<String, JsonSerializer> serializers = new HashMap<>();

    public JsonSerializerRegistry() {
        register(DEFAULT_SERIALIZER, new GsonSerializer(new GsonBuilder().create()));
    }

    /**
     * Returns the JSON serializer for the given id. Throws an error if the serializer
     * is not present.
     */
    @NotNull
    public JsonSerializer getSerializer(String id) {
        if (!serializers.containsKey(id)) {
            throw new IllegalArgumentException("Could not find JSON serializer with id '" + id + "'");
        }
        return serializers.get(id);
    }

    /**
     * Registers a new serializer with the given id.
     * <p>
     * Implementations are encouraged to pick a unique name that does not equal the type
     * of serializer to properly support other implementations.
     * <p>
     * You are encouraged to register custom serialization handlers for specific classes onto
     * your own serializer instance.
     */
    public void register(String id, JsonSerializer serializer) {
        Preconditions.checkState(!serializers.containsKey(id), "Cannot register serializer " + id + " twice");
        serializers.put(id, serializer);
    }
}
