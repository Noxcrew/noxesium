package com.noxcrew.noxesium.api.component;

import net.kyori.adventure.key.Key;

import java.util.Objects;

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
public record NoxesiumComponentType<T>(Key id, Class<T> clazz) {

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NoxesiumComponentType<?> that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
