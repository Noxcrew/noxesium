package com.noxcrew.noxesium.api.fabric.registry;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.component.NoxesiumComponentListener;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.qib.QibDefinition;
import com.noxcrew.noxesium.api.registry.ClientNoxesiumRegistry;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import java.util.List;
import net.kyori.adventure.key.Key;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Stores the different Noxesium registries.
 */
public class NoxesiumRegistries {
    /**
     * Stores all different effects qibs can have.
     */
    public static ClientNoxesiumRegistry<QibDefinition> QIB_EFFECTS = new ClientNoxesiumRegistry<>();

    /**
     * Defines all components that can be applied to the entire game, allowing them to control
     * or modify game behavior.
     */
    public static ClientNoxesiumRegistry<NoxesiumComponentType<?>> GAME_COMPONENTS = new ClientNoxesiumRegistry<>();

    /**
     * Defines custom entity components that can be read from any entity's NBT data.
     */
    public static ClientNoxesiumRegistry<NoxesiumComponentType<?>> ENTITY_COMPONENTS = new ClientNoxesiumRegistry<>();

    /**
     * Defines custom item components that can be read from any item's NBT data.
     */
    public static ClientNoxesiumRegistry<NoxesiumComponentType<?>> ITEM_COMPONENTS = new ClientNoxesiumRegistry<>();

    /**
     * Defines custom block entity components that can be read from any block entity's NBT data.
     */
    public static ClientNoxesiumRegistry<NoxesiumComponentType<?>> BLOCK_ENTITY_COMPONENTS =
            new ClientNoxesiumRegistry<>();

    /** All main registries. */
    public static List<ClientNoxesiumRegistry<?>> REGISTRIES =
            List.of(QIB_EFFECTS, GAME_COMPONENTS, ENTITY_COMPONENTS, ITEM_COMPONENTS, BLOCK_ENTITY_COMPONENTS);

    /**
     * Registers a new component type to the registry.
     */
    public static <C> NoxesiumComponentType<C> register(
            RegistryCollection<NoxesiumComponentType<?>> collection,
            String namespace,
            String key,
            Codec<C> codec,
            StreamCodec<? super RegistryFriendlyByteBuf, C> streamCodec,
            NoxesiumComponentListener<C, ?> listener) {
        var component = new NoxesiumComponentType<>(namespace, key, codec, streamCodec, listener);
        collection.register(Key.key(namespace, key), component);
        return component;
    }
}
