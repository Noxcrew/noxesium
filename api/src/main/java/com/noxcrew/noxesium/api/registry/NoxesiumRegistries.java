package com.noxcrew.noxesium.api.registry;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.NoxesiumSide;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.qib.QibDefinition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.key.Key;

/**
 * Stores the different Noxesium registries.
 */
public class NoxesiumRegistries {
    /**
     * Stores all different effects qibs can have.
     */
    public static NoxesiumRegistry<QibDefinition> QIB_EFFECTS = createRegistry("qib_effects", true);

    /**
     * Defines all components that can be applied to the entire game, allowing them to control
     * or modify game behavior.
     */
    public static NoxesiumRegistry<NoxesiumComponentType<?>> GAME_COMPONENTS = createRegistry("game_components", true);

    /**
     * Defines custom entity components that can be read from any entity's NBT data.
     */
    public static NoxesiumRegistry<NoxesiumComponentType<?>> ENTITY_COMPONENTS = createRegistry("entity_components", true);

    /**
     * Defines custom item components that can be read from any item's NBT data.
     */
    public static NoxesiumRegistry<NoxesiumComponentType<?>> ITEM_COMPONENTS = createRegistry("item_components", false);

    /**
     * Defines custom block entity components that can be read from any block entity's NBT data.
     */
    public static NoxesiumRegistry<NoxesiumComponentType<?>> BLOCK_ENTITY_COMPONENTS =
            createRegistry("block_entity_components", false);

    /**
     * All main registries.
     */
    public static List<NoxesiumRegistry<?>> REGISTRIES =
            List.of(QIB_EFFECTS, GAME_COMPONENTS, ENTITY_COMPONENTS, ITEM_COMPONENTS, BLOCK_ENTITY_COMPONENTS);

    /**
     * All main registries mapped by their identifier.
     */
    public static Map<Key, NoxesiumRegistry<?>> REGISTRIES_BY_ID = new HashMap<>();

    static {
        for (var registry : REGISTRIES) {
            REGISTRIES_BY_ID.put(registry.id(), registry);
        }
    }

    /**
     * Creates a new registry based on the side this code is running on.
     */
    public static <T> NoxesiumRegistry<T> createRegistry(String key, boolean synchronize) {
        if (!synchronize) {
            return new NoxesiumRegistry<>(Key.key(NoxesiumReferences.NAMESPACE, key));
        } else if (NoxesiumApi.getInstance().getSide() == NoxesiumSide.CLIENT) {
            return new ClientNoxesiumRegistry<>(Key.key(NoxesiumReferences.NAMESPACE, key));
        } else {
            return new ServerNoxesiumRegistry<>(Key.key(NoxesiumReferences.NAMESPACE, key));
        }
    }

    /**
     * Registers a new component type to the registry.
     */
    public static <C> NoxesiumComponentType<C> register(
            RegistryCollection<NoxesiumComponentType<?>> collection, String namespace, String key) {
        var component = new NoxesiumComponentType<C>(Key.key(namespace, key));
        collection.register(Key.key(namespace, key), component);
        return component;
    }
}
