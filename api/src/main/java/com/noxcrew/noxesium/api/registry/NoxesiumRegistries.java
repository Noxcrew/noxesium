package com.noxcrew.noxesium.api.registry;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.NoxesiumSide;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.feature.qib.QibDefinition;
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
    public static NoxesiumRegistry<QibDefinition> QIB_EFFECTS = createRegistry("qib_effects", true, true);

    /**
     * Defines all components that can be applied to the entire game, allowing them to control
     * or modify game behavior.
     */
    public static NoxesiumRegistry<NoxesiumComponentType<?>> GAME_COMPONENTS =
            createRegistry("game_components", true, false);

    /**
     * Defines custom entity components that can be read from any entity's NBT data.
     */
    public static NoxesiumRegistry<NoxesiumComponentType<?>> ENTITY_COMPONENTS =
            createRegistry("entity_components", true, false);

    /**
     * Defines custom item components that can be read from any item's NBT data.
     */
    public static NoxesiumRegistry<NoxesiumComponentType<?>> ITEM_COMPONENTS =
            createRegistry("item_components", false, false);

    /**
     * Defines custom block entity components that can be read from any block entity's NBT data.
     * <p>
     * It should be noted that this is only supported for block entities that send data packets, that is
     * any block entity that also stores some data or state. As of 1.21.8 these are:
     * Banner, Beacon, Bed, Brushable (Suspicious Gravel/Sand), Campfire, Conduit, Creaking Heart,
     * Decorated Pot, Jigsaw, Sign, Skull, Spawner, Structure Block, Test Block, End Gateway, Trial Spawner, Trial Vault
     * <p>
     * Importantly, containers are not included as their data is not sent at all times!
     */
    public static NoxesiumRegistry<NoxesiumComponentType<?>> BLOCK_ENTITY_COMPONENTS =
            createRegistry("block_entity_components", true, false);

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
     *
     * @param network     A network registry is a registry whose contents need to be sent to
     *                    a client in their own packets. The client has to populate the registry
     *                    itself, only the identifiers are synced.
     * @param synchronize A synchronized registry is a registry that is fully synchronized to
     *                    clients, including the definitions of the values themselves.
     */
    public static <T> NoxesiumRegistry<T> createRegistry(String key, boolean network, boolean synchronize) {
        var id = Key.key(NoxesiumReferences.NAMESPACE, key);
        if (network && synchronize && NoxesiumApi.getInstance().getSide() == NoxesiumSide.SERVER) {
            return new SynchronizedServerNoxesiumRegistry<>(id);
        } else if (!network) {
            return new NoxesiumRegistry<>(id);
        } else if (NoxesiumApi.getInstance().getSide() == NoxesiumSide.CLIENT) {
            return new ClientNoxesiumRegistry<>(id);
        } else {
            return new ServerNoxesiumRegistry<>(id);
        }
    }

    /**
     * Registers a new component type to the registry.
     */
    public static <T> NoxesiumComponentType<T> register(
            RegistryCollection<NoxesiumComponentType<?>> collection, String namespace, String key, Class<T> clazz) {
        var component = new NoxesiumComponentType<T>(Key.key(namespace, key), clazz);
        collection.register(Key.key(namespace, key), component);
        return component;
    }
}
