package com.noxcrew.noxesium.fabric.mixin.rules.server;

import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentHolder;
import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumRegistries;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Minecraft.class)
public class MinecraftComponentMixin implements NoxesiumComponentHolder {
    @Unique
    private Map<NoxesiumComponentType<?>, Object> noxesium$components = null;

    @Override
    public void noxesium$clearComponents() {
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
    public void noxesium$loadComponent(int index, Object value) {
        var component = NoxesiumRegistries.GAME_COMPONENTS.getById(index);
        if (component == null) return;
        if (noxesium$components == null) noxesium$components = new ConcurrentHashMap<>();
        noxesium$components.put(component, value);
    }

    @Override
    public void noxesium$unsetComponent(int index) {
        if (noxesium$components == null) return;
        var component = NoxesiumRegistries.GAME_COMPONENTS.getById(index);
        if (component == null) return;
        noxesium$components.remove(component);
        if (noxesium$components.isEmpty()) {
            noxesium$components = null;
        }
    }
}
