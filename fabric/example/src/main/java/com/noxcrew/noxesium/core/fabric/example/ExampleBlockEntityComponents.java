package com.noxcrew.noxesium.core.fabric.example;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import net.minecraft.util.Unit;

/**
 * Stores an example custom block entity component.
 */
public class ExampleBlockEntityComponents {
    public static final RegistryCollection<NoxesiumComponentType<?>> INSTANCE =
            new RegistryCollection<>(NoxesiumRegistries.BLOCK_ENTITY_COMPONENTS);

    /**
     * Makes a block entity invisible.
     */
    public static NoxesiumComponentType<Unit> INVISIBLE = register("invisible", Unit.CODEC);

    /**
     * Registers a new component type to the registry.
     */
    private static <T> NoxesiumComponentType<T> register(String key, Codec<T> codec) {
        return NoxesiumRegistries.register(INSTANCE, NoxesiumReferences.NAMESPACE, key, codec, null, null);
    }
}
