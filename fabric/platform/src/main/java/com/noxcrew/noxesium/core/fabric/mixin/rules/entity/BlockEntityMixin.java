package com.noxcrew.noxesium.core.fabric.mixin.rules.entity;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.NoxesiumComponentHolder;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.nms.registry.ComponentSerializerRegistry;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.core.fabric.component.CachedComponentData;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements NoxesiumComponentHolder {

    @Unique
    private Map<NoxesiumComponentType<?>, CachedComponentData<?>> noxesium$componentCache = null;

    @Override
    public <T> @Nullable T noxesium$getComponent(NoxesiumComponentType<T> component) {
        if (noxesium$componentCache != null) {
            var cache = noxesium$componentCache.get(component);
            if (cache != null) {
                return (T) cache.load();
            }
        }
        return null;
    }

    @Override
    public boolean noxesium$hasComponent(NoxesiumComponentType<?> component) {
        return noxesium$componentCache != null && noxesium$componentCache.containsKey(component);
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    public void onLoadFromTag(ValueInput valueInput, CallbackInfo ci) {
        noxesium$componentCache = null;

        // Parse the entire object under the Noxesium group in the custom data
        var noxesiumData = valueInput.read(NoxesiumReferences.COMPONENT_NAMESPACE, CompoundTag.CODEC);
        if (noxesiumData.isPresent()) {
            for (var entry : noxesiumData.get().entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();

                // Ignore keys we cannot deserialize
                var type = NoxesiumRegistries.BLOCK_ENTITY_COMPONENTS.getByKey(Key.key(key));
                if (type == null) continue;
                var serializer =
                        ComponentSerializerRegistry.getSerializers(NoxesiumRegistries.BLOCK_ENTITY_COMPONENTS, type);
                if (serializer == null) continue;

                // Store that we know about this data
                if (noxesium$componentCache == null) noxesium$componentCache = new HashMap<>();
                noxesium$componentCache.put(type, new CachedComponentData<>(value, serializer.codec()));
            }
        }
    }
}
