package com.noxcrew.noxesium.core.nms.registry;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * Stores all common Noxesium game component types that rely on NMS code.
 */
public class NmsGameComponentTypes {
    public static final RegistryCollection<NoxesiumComponentType<?>> INSTANCE =
            new RegistryCollection<>(NoxesiumRegistries.GAME_COMPONENTS);

    /**
     * Allows overriding the item that is used when resolving the capabilities of an empty item slot.
     */
    public static NoxesiumComponentType<ItemStack> HAND_ITEM_OVERRIDE = register("hand_item_override", ItemStack.class);

    /**
     * Defines a list of items to show in a custom creative tab.
     */
    public static NoxesiumComponentType<List<ItemStack>> CUSTOM_CREATIVE_ITEMS =
            register("custom_creative_items", List.class);

    /**
     * Registers a new component type to the registry.
     */
    private static <T, R> NoxesiumComponentType<T> register(String key, Class<R> clazz) {
        return NoxesiumRegistries.register(INSTANCE, NoxesiumReferences.NAMESPACE, key, (Class<T>) clazz);
    }
}
