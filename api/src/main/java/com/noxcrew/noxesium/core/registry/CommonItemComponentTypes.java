package com.noxcrew.noxesium.core.registry;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import com.noxcrew.noxesium.api.util.Unit;
import com.noxcrew.noxesium.core.feature.item.HoverSound;
import com.noxcrew.noxesium.core.feature.item.Hoverable;

/**
 * Stores all common Noxesium item component types.
 */
public class CommonItemComponentTypes {
    public static final RegistryCollection<NoxesiumComponentType<?>> INSTANCE =
            new RegistryCollection<>(NoxesiumRegistries.ITEM_COMPONENTS);

    /**
     * If set, prevents the item stack from being picked up. Prevents flickering when clicking
     * around in a menu.
     */
    public static NoxesiumComponentType<Unit> IMMOVABLE = register("immovable", Unit.class);

    /**
     * Defines a sound effect that is played when hovering over an item.
     */
    public static NoxesiumComponentType<HoverSound> HOVER_SOUND = register("hover_sound", HoverSound.class);

    /**
     * Customises the slot hover rendering for this item slot.
     */
    public static NoxesiumComponentType<Hoverable> HOVERABLE = register("hoverable", Hoverable.class);

    /**
     * Registers a new component type to the registry.
     */
    private static <T> NoxesiumComponentType<T> register(String key, Class<T> clazz) {
        return NoxesiumRegistries.register(INSTANCE, NoxesiumReferences.NAMESPACE, key, clazz);
    }
}
