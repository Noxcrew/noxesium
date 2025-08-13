package com.noxcrew.noxesium.fabric.mixin.rules.entity;

import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentHolder;
import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumRegistries;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Implements [NoxesiumComponentHolder] onto the Entity class.
 */
@Mixin(Entity.class)
public abstract class EntityComponentMixin implements NoxesiumComponentHolder {

    @Unique
    private Map<NoxesiumComponentType<?>, Object> noxesium$components = null;

    @Override
    public <T> @Nullable T noxesium$getComponent(NoxesiumComponentType<T> component) {
        if (noxesium$components != null) {
            return (T) noxesium$components.get(component);
        }
        return null;
    }

    @Override
    public boolean noxesium$hasComponent(NoxesiumComponentType<?> component) {
        return noxesium$components != null && noxesium$components.containsKey(component);
    }

    @Override
    public void noxesium$loadComponent(int index, Object value) {
        var component = NoxesiumRegistries.ENTITY_COMPONENTS.getById(index);
        if (component == null) return;
        if (noxesium$components == null) noxesium$components = new ConcurrentHashMap<>();
        noxesium$components.put(component, value);
    }

    @Override
    public void noxesium$unsetComponent(int index) {
        if (noxesium$components == null) return;
        var component = NoxesiumRegistries.ENTITY_COMPONENTS.getById(index);
        if (component == null) return;
        noxesium$components.remove(component);
        if (noxesium$components.isEmpty()) {
            noxesium$components = null;
        }
    }

    @Shadow
    public abstract void setBoundingBox(AABB aABB);

    @Shadow
    protected abstract AABB makeBoundingBox();
}
