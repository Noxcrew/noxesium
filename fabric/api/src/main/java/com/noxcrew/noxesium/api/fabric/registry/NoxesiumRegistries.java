package com.noxcrew.noxesium.api.fabric.registry;

import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.qib.QibDefinition;
import com.noxcrew.noxesium.api.registry.ClientNoxesiumRegistry;
import java.util.List;

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
}
