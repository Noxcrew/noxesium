package com.noxcrew.noxesium.api.fabric.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * A Noxesium component is a custom version of Mojang's Data Component system which allows
 * any arbitrary data to be added to an entity, item, block entity, or to the entire game.
 * <p>
 * Components do not have any inherit behavior but are cached and can be fetched from any
 * game logic to customise the interactions of the owning object.
 * <p>
 * Game and entity components are stored in NBT on the server-side and synced to the client
 * in their own packets while item and block entity components are de-serialized on the
 * client itself.
 * <p>
 * Component types are defined in a custom registry similar to vanilla components.
 */
public record NoxesiumComponentType<T>(
        ResourceLocation id, Codec<T> codec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {

    /**
     * Creates a new component type automatically for the given namespace and key with the default encoder cache enabled.
     */
    public NoxesiumComponentType(
            String namespace,
            String key,
            Codec<T> codec,
            @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        this(
                ResourceLocation.fromNamespaceAndPath(namespace, key),
                DataComponents.ENCODER_CACHE.wrap(codec),
                streamCodec);
    }
}
