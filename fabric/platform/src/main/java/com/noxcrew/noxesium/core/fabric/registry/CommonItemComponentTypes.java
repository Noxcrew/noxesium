package com.noxcrew.noxesium.core.fabric.registry;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import com.noxcrew.noxesium.core.fabric.feature.item.HoverSound;
import com.noxcrew.noxesium.core.fabric.feature.item.Hoverable;
import net.minecraft.util.Unit;

/**
 * Stores all Noxesium item component types.
 */
public class CommonItemComponentTypes {
    public static final RegistryCollection<NoxesiumComponentType<?>> INSTANCE =
            new RegistryCollection<>(NoxesiumRegistries.ITEM_COMPONENTS);

    /**
     * If set, prevents the item stack from being picked up. Prevents flickering when clicking
     * around in a menu.
     */
    public static NoxesiumComponentType<Unit> IMMOVABLE = register("immovable", Unit.CODEC);

    /**
     * Defines a sound effect that is played when hovering over an item.
     */
    public static NoxesiumComponentType<HoverSound> HOVER_SOUND = register("hover_sound", HoverSound.CODEC);

    /**
     * Customises the slot hover rendering for this item slot.
     */
    public static NoxesiumComponentType<Hoverable> HOVERABLE = register("hoverable", Hoverable.CODEC);

    /**
     * Registers a new component type to the registry.
     */
    private static <T> NoxesiumComponentType<T> register(String key, Codec<T> codec) {
        return NoxesiumRegistries.register(INSTANCE, NoxesiumReferences.NAMESPACE, key, codec, null, null);
    }
}
