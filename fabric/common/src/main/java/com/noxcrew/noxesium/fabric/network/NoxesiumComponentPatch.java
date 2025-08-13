package com.noxcrew.noxesium.fabric.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentHolder;
import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.key.Key;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Stores a collection of components to be applied to or removed from a holder.
 */
public class NoxesiumComponentPatch {
    /**
     * Creates a new codec for a patch on the given registry.
     */
    public static Codec<NoxesiumComponentPatch> codec(NoxesiumRegistry<NoxesiumComponentType<?>> registry) {
        var componentTypeCodec = Codec.STRING.<NoxesiumComponentType<?>>flatXmap(
                key -> {
                    var resource = Key.key(key);
                    var componentType = registry.getByKey(resource);
                    if (componentType == null) {
                        return DataResult.error(() -> "No component with type: '" + resource + "'");
                    } else {
                        return DataResult.success(componentType);
                    }
                },
                type -> DataResult.success(type.id().toString()));

        return Codec.<NoxesiumComponentType<?>, Object>dispatchedMap(componentTypeCodec, NoxesiumComponentType::codec)
                .xmap(
                        map -> {
                            if (map.isEmpty()) {
                                return new NoxesiumComponentPatch(new ConcurrentHashMap<>());
                            } else {
                                var patch = new ConcurrentHashMap<NoxesiumComponentType<?>, Optional<?>>(map.size());
                                for (var entry : map.entrySet()) {
                                    patch.put(entry.getKey(), Optional.ofNullable(entry.getValue()));
                                }
                                return new NoxesiumComponentPatch(patch);
                            }
                        },
                        patch -> {
                            if (patch.isEmpty()) {
                                return new HashMap<>();
                            } else {
                                var map = new ConcurrentHashMap<NoxesiumComponentType<?>, Object>(
                                        patch.getMap().size());
                                for (var entry : patch.getMap().entrySet()) {
                                    // Ignore empty values in patches when serializing! Empty values are used only in
                                    // the stream codec to indicate values to remove. The codec is used for saving to
                                    // the disk on the server-side where we only save the actual values.
                                    if (entry.getValue().isEmpty()) continue;
                                    map.put(entry.getKey(), entry.getValue().orElseThrow());
                                }
                                return map;
                            }
                        });
    }

    /**
     * Creates a new stream codec for a patch on the given registry.
     */
    public static StreamCodec<RegistryFriendlyByteBuf, NoxesiumComponentPatch> streamCodec(
            NoxesiumRegistry<NoxesiumComponentType<?>> registry) {
        return new StreamCodec<>() {
            @Override
            public NoxesiumComponentPatch decode(RegistryFriendlyByteBuf buffer) {
                var updated = buffer.readVarInt();
                var removed = buffer.readVarInt();

                if (updated == 0 && removed == 0) {
                    return new NoxesiumComponentPatch(new HashMap<>());
                } else {
                    var total = updated + removed;
                    var map = new ConcurrentHashMap<NoxesiumComponentType<?>, Optional<?>>(total);

                    for (int i = 0; i < updated; i++) {
                        var index = buffer.readVarInt();
                        var type = registry.getById(index);
                        if (type == null) {
                            throw new IllegalArgumentException(
                                    "Received invalid component id '" + index + "' not found in registry");
                        }
                        if (type.streamCodec() == null) {
                            throw new IllegalArgumentException("Received component type '" + type.id()
                                    + "' that does not have a stream codec defined");
                        }
                        var decoded = type.streamCodec().decode(buffer);
                        map.put(type, Optional.of(decoded));
                    }

                    for (int i = 0; i < removed; i++) {
                        var index = buffer.readVarInt();
                        var type = registry.getById(index);
                        if (type == null) {
                            throw new IllegalArgumentException(
                                    "Received invalid component id '" + index + "' not found in registry");
                        }
                        map.put(type, Optional.empty());
                    }

                    return new NoxesiumComponentPatch(map);
                }
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, NoxesiumComponentPatch patch) {
                if (patch.isEmpty()) {
                    buffer.writeVarInt(0);
                    buffer.writeVarInt(0);
                } else {
                    // Start by writing how many of each type there are
                    var updated = 0;
                    var removed = 0;

                    for (var entry : patch.getMap().entrySet()) {
                        if (entry.getValue().isPresent()) {
                            updated++;
                        } else {
                            removed++;
                        }
                    }

                    buffer.writeVarInt(updated);
                    buffer.writeVarInt(removed);

                    // Write the components themselves
                    for (var entry : patch.getMap().entrySet()) {
                        if (entry.getValue().isPresent()) {
                            buffer.writeVarInt(registry.getIdFor(entry.getKey()));
                            encodeComponent(
                                    buffer, entry.getKey(), entry.getValue().get());
                        }
                    }
                    for (var entry : patch.getMap().entrySet()) {
                        if (entry.getValue().isEmpty()) {
                            buffer.writeVarInt(registry.getIdFor(entry.getKey()));
                        }
                    }
                }
            }

            private <T> void encodeComponent(
                    RegistryFriendlyByteBuf buffer, NoxesiumComponentType<T> type, Object raw) {
                type.streamCodec().cast().encode(buffer, (T) raw);
            }
        };
    }

    private final Map<NoxesiumComponentType<?>, Optional<?>> data;

    public NoxesiumComponentPatch(Map<NoxesiumComponentType<?>, Optional<?>> data) {
        this.data = data;
    }

    /**
     * Returns whether this patch is empty.
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Returns the raw contents of this patch.
     */
    public Map<NoxesiumComponentType<?>, Optional<?>> getMap() {
        return data;
    }

    /**
     * Applies this patch to the given holder.
     */
    public void apply(NoxesiumComponentHolder holder) {
        for (var entry : data.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (value.isEmpty()) {
                if (key.listener() != null && key.listener().hasListeners()) {
                    var oldValue = holder.noxesium$getComponent(key);
                    holder.noxesium$unsetComponent(key);
                    key.listener().trigger(holder, oldValue, null);
                } else {
                    holder.noxesium$unsetComponent(key);
                }
            } else {
                if (key.listener() != null && key.listener().hasListeners()) {
                    var oldValue = holder.noxesium$getComponent(key);
                    var newValue = value.orElse(null);
                    holder.noxesium$loadComponent(key, newValue);
                    key.listener().trigger(holder, oldValue, newValue);
                } else {
                    holder.noxesium$loadComponent(key, value.get());
                }
            }
        }
    }
}
