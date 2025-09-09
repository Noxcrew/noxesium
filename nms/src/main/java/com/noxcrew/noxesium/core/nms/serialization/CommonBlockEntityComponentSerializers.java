package com.noxcrew.noxesium.core.nms.serialization;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.component.NoxesiumComponentListener;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.nms.serialization.ComponentSerializerRegistry;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.core.registry.CommonBlockEntityComponentTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Registers the serializers for different block entity components.
 */
public class CommonBlockEntityComponentSerializers {
    /**
     * Registers all serializers.
     */
    public static void register() {
        register(CommonBlockEntityComponentTypes.BEACON_BEAM_HEIGHT, Codec.INT, ByteBufCodecs.VAR_INT);
    }

    /**
     * Registers a new serializer to the registry.
     */
    private static <T> void register(
            NoxesiumComponentType<T> component,
            Codec<T> codec,
            StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        ComponentSerializerRegistry.registerSerializers(
                NoxesiumRegistries.BLOCK_ENTITY_COMPONENTS,
                component,
                codec,
                streamCodec,
                new NoxesiumComponentListener<T, BlockEntity>());
    }
}
