package com.noxcrew.noxesium.core.registry;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import com.noxcrew.noxesium.api.util.Unit;
import java.awt.Color;
import org.joml.Vector3f;

/**
 * Stores all common Noxesium entity component types.
 */
public class CommonEntityComponentTypes {
    public static final RegistryCollection<NoxesiumComponentType<?>> INSTANCE =
            new RegistryCollection<>(NoxesiumRegistries.ENTITY_COMPONENTS);

    /**
     * If set, bubbles are removed from guardian beams shot by this entity.
     */
    public static NoxesiumComponentType<Unit> DISABLE_BUBBLES = register("disable_bubbles", Unit.class);

    /**
     * Defines a color to use for a beam created by this entity. Applies to guardian beams
     * and end crystal beams.
     */
    public static NoxesiumComponentType<Color> BEAM_COLOR = register("beam_color", Color.class);

    /**
     * Defines a color used in combination with [BEAM_COLOR] to create a linear fade.
     */
    public static NoxesiumComponentType<Color> BEAM_COLOR_FADE = register("beam_color_fade", Color.class);

    /**
     * Allows defining qib behavior for an interaction entity. You can find more information
     * about the qib system in the qib package. This has to be an identifier present in the
     * qib behavior registry!
     */
    public static NoxesiumComponentType<String> QIB_BEHAVIOR = register("qib_behavior", String.class);

    /**
     * Defines a color to use for the glowing outline of this entity.
     */
    public static NoxesiumComponentType<Color> GLOW_COLOR = register("glow_color", Color.class);

    /**
     * Defines a hitbox to use for this entity to override the regular hitbox.
     */
    public static NoxesiumComponentType<Vector3f> HITBOX_OVERRIDE = register("hitbox_override", Vector3f.class);

    /**
     * Defines a color to use for the hitbox rendering.
     */
    public static NoxesiumComponentType<Color> HITBOX_COLOR = register("hitbox_color", Color.class);

    /**
     * Registers a new component type to the registry.
     */
    private static <T> NoxesiumComponentType<T> register(String key, Class<T> clazz) {
        return NoxesiumRegistries.register(INSTANCE, NoxesiumReferences.NAMESPACE, key, clazz);
    }
}
