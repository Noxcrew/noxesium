package com.noxcrew.noxesium.core.nms.serialization;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.component.NoxesiumComponentListener;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.nms.codec.NoxesiumCodecs;
import com.noxcrew.noxesium.api.nms.codec.NoxesiumStreamCodecs;
import com.noxcrew.noxesium.api.nms.serialization.ComponentSerializerRegistry;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

/**
 * Registers the serializers for different entity components.
 */
public class CommonEntityComponentSerializers {
    /**
     * Registers all serializers.
     */
    public static void register() {
        register(CommonEntityComponentTypes.DISABLE_BUBBLES, NoxesiumCodecs.UNIT, NoxesiumStreamCodecs.UNIT);
        register(CommonEntityComponentTypes.BEAM_COLOR, NoxesiumCodecs.COLOR_ARGB, NoxesiumStreamCodecs.COLOR_ARGB);
        register(
                CommonEntityComponentTypes.BEAM_COLOR_FADE, NoxesiumCodecs.COLOR_ARGB, NoxesiumStreamCodecs.COLOR_ARGB);
        register(
                CommonEntityComponentTypes.QIB_BEHAVIOR,
                NoxesiumCodecs.KEY,
                NoxesiumStreamCodecs.registryKey(NoxesiumRegistries.QIB_EFFECTS));
        register(CommonEntityComponentTypes.GLOW_COLOR, NoxesiumCodecs.COLOR_ARGB, NoxesiumStreamCodecs.COLOR_ARGB);
        register(CommonEntityComponentTypes.HITBOX_OVERRIDE, ExtraCodecs.VECTOR3F, ByteBufCodecs.VECTOR3F);
        register(CommonEntityComponentTypes.HITBOX_COLOR, NoxesiumCodecs.COLOR_ARGB, NoxesiumStreamCodecs.COLOR_ARGB);
    }

    /**
     * Registers a new serializer to the registry.
     */
    private static <T> void register(
            NoxesiumComponentType<T> component,
            Codec<T> codec,
            StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        ComponentSerializerRegistry.registerSerializers(
                NoxesiumRegistries.ENTITY_COMPONENTS,
                component,
                codec,
                streamCodec,
                new NoxesiumComponentListener<T, Entity>());
    }
}
