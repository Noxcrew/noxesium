package com.noxcrew.noxesium.fabric.registry;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentListener;
import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumExtraCodecs;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumExtraStreamCodecs;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumRegistries;
import java.awt.Color;
import net.kyori.adventure.key.Key;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

/**
 * Stores all Noxesium entity component types.
 */
public class CommonEntityComponentTypes {

    /**
     * If set, bubbles are removed from guardian beams shot by this entity.
     */
    public static NoxesiumComponentType<Unit> DISABLE_BUBBLES =
            register("disable_bubbles", Unit.CODEC, Unit.STREAM_CODEC);

    /**
     * Defines a color to use for a beam created by this entity. Applies to guardian beams
     * and end crystal beams.
     */
    public static NoxesiumComponentType<Color> BEAM_COLOR =
            register("beam_color", NoxesiumExtraCodecs.COLOR_ARGB, NoxesiumExtraStreamCodecs.COLOR_ARGB);

    /**
     * Defines a color used in combination with [BEAM_COLOR] to create a linear fade.
     */
    public static NoxesiumComponentType<Color> BEAM_COLOR_FADE =
            register("beam_color_fade", NoxesiumExtraCodecs.COLOR_ARGB, NoxesiumExtraStreamCodecs.COLOR_ARGB);

    /**
     * Allows defining qib behavior for an interaction entity. You can find more information
     * about the qib system in the qib package. This can either be the identifier of a behavior
     * as found in the registry or an inlined behavior.
     */
    public static NoxesiumComponentType<String> QIB_BEHAVIOR =
            register("qib_behavior", Codec.STRING, ByteBufCodecs.STRING_UTF8);

    /**
     * Defines a color to use for the glowing outline of this entity.
     */
    public static NoxesiumComponentType<Color> GLOW_COLOR =
            register("glow_color", NoxesiumExtraCodecs.COLOR_ARGB, NoxesiumExtraStreamCodecs.COLOR_ARGB);

    /**
     * Defines a hitbox to use for this entity to override the regular hitbox.
     */
    public static NoxesiumComponentType<Vector3f> HITBOX_OVERRIDE =
            register("hitbox_override", ExtraCodecs.VECTOR3F, ByteBufCodecs.VECTOR3F);

    /**
     * Registers a new component type to the registry.
     */
    private static <T> NoxesiumComponentType<T> register(
            String key, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return NoxesiumRegistries.ENTITY_COMPONENTS.register(
                Key.key(NoxesiumReferences.NAMESPACE, key),
                new NoxesiumComponentType<T>(
                        NoxesiumReferences.NAMESPACE,
                        key,
                        codec,
                        streamCodec,
                        new NoxesiumComponentListener<T, Entity>()));
    }
}
