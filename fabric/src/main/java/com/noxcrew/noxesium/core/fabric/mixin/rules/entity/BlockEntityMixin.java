package com.noxcrew.noxesium.core.fabric.mixin.rules.entity;

import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.component.RemoteNoxesiumComponentHolder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements RemoteNoxesiumComponentHolder {

    @Unique
    private Map<NoxesiumComponentType<?>, Object> noxesium$components = null;

    @Override
    public void noxesium$reloadComponents() {
        noxesium$components = null;
    }

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
    public void noxesium$loadComponent(NoxesiumComponentType<?> component, Object value) {
        if (value == null) {
            noxesium$unsetComponent(component);
            return;
        }
        if (noxesium$components == null) noxesium$components = new ConcurrentHashMap<>();
        noxesium$components.put(component, value);
    }

    @Override
    public void noxesium$unsetComponent(NoxesiumComponentType<?> component) {
        if (noxesium$components == null) return;
        noxesium$components.remove(component);
        if (noxesium$components.isEmpty()) {
            noxesium$components = null;
        }
    }
}
