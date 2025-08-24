package com.noxcrew.noxesium.api.component;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Manages the components for all entities.
 */
public abstract class NoxesiumEntityManager<E, C extends MutableNoxesiumComponentHolder> {
    protected static NoxesiumEntityManager<?, ?> instance;

    /**
     * Returns the singleton instance of this class.
     */
    public static <E, C extends MutableNoxesiumComponentHolder> NoxesiumEntityManager<E, C> getInstance() {
        Preconditions.checkNotNull(instance, "Cannot get entity manager instance before it is defined");
        return (NoxesiumEntityManager<E, C>) instance;
    }

    /**
     * Sets the player manager instance.
     */
    public static void setInstance(NoxesiumEntityManager<?, ?> instance) {
        Preconditions.checkState(
                NoxesiumEntityManager.instance == null, "Cannot set the entity manager instance twice!");
        NoxesiumEntityManager.instance = instance;
    }

    private final Map<E, C> entities = new WeakHashMap<>();

    /**
     * Returns the component holder for the given entity, if no
     * holder has been fetched it will be built using components
     * stored in the entity's NBT data.
     *
     * This method should only be used if the data will be changed
     * after this to avoid unnecessarily fetching it.
     */
    @NotNull
    public C getComponentHolder(@NotNull E entity) {
        return entities.computeIfAbsent(entity, this::loadComponentHolder);
    }

    /**
     * Returns the component holder for the given entity only
     * if the entity already has some data.
     */
    @Nullable
    public C getComponentHolderIfPresent(@NotNull E entity) {
        if (entities.containsKey(entity)) return entities.get(entity);
        if (!hasComponents(entity)) return null;
        return getComponentHolder(entity);
    }

    /**
     * Evicts the data stored for the given entity.
     */
    public void evictEntity(@Nullable E entity) {
        if (entity == null) return;
        entities.remove(entity);
    }

    /**
     * Returns the collection of all entities with data.
     */
    public Collection<C> getAllEntities() {
        return entities.values();
    }

    /**
     * Loads a new component holder for the given entity.
     */
    protected abstract C loadComponentHolder(@NotNull E entity);

    /**
     * Returns whether the given entity has components.
     */
    protected abstract boolean hasComponents(@NotNull E entity);
}
