package com.noxcrew.noxesium.core.fabric.mixin.rules.item;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.NoxesiumComponentHolder;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.nms.registry.ComponentSerializerRegistry;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.core.fabric.component.CachedComponentData;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin implements NoxesiumComponentHolder {
    @Unique
    private Map<NoxesiumComponentType<?>, CachedComponentData<?>> noxesium$componentCache = null;

    @Unique
    private boolean noxesium$loaded = false;

    @Unique
    private void noxesium$loadComponentsIfMissing() {
        if (!noxesium$loaded) {
            noxesium$loaded = true;

            // Parse the entire object under the Noxesium group in the custom data
            var itemStack = (ItemStack) (Object) this;
            var customData = itemStack.get(DataComponents.CUSTOM_DATA);
            if (customData == null) return;
            var noxesiumData = customData.getUnsafe().getCompound(NoxesiumReferences.COMPONENT_NAMESPACE);
            if (noxesiumData.isPresent()) {
                for (var entry : noxesiumData.get().entrySet()) {
                    var key = entry.getKey();
                    var value = entry.getValue();

                    // Ignore keys we cannot deserialize
                    var type = NoxesiumRegistries.ITEM_COMPONENTS.getByKey(Key.key(key));
                    if (type == null) continue;
                    var serializer =
                            ComponentSerializerRegistry.getSerializers(NoxesiumRegistries.ITEM_COMPONENTS, type);
                    if (serializer == null) continue;

                    // Store that we know about this data
                    if (noxesium$componentCache == null) noxesium$componentCache = new HashMap<>();
                    noxesium$componentCache.put(type, new CachedComponentData<>(value, serializer.codec()));
                }
            }
        }
    }

    @Unique
    public void noxesium$reloadComponents() {
        noxesium$componentCache = null;
        noxesium$loaded = false;
    }

    @Override
    public <T> @Nullable T noxesium$getComponent(NoxesiumComponentType<T> component) {
        noxesium$loadComponentsIfMissing();
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
        noxesium$loadComponentsIfMissing();
        return noxesium$componentCache != null && noxesium$componentCache.containsKey(component);
    }

    @Inject(method = "applyComponents(Lnet/minecraft/core/component/DataComponentMap;)V", at = @At("RETURN"))
    public void onApplyComponents(DataComponentMap dataComponentMap, CallbackInfo ci) {
        if (dataComponentMap.has(DataComponents.CUSTOM_DATA)) {
            noxesium$reloadComponents();
        }
    }

    @Inject(method = "set", at = @At("RETURN"))
    public <T> void onSetComponent(DataComponentType<T> dataComponentType, T object, CallbackInfoReturnable<T> cir) {
        if (dataComponentType == DataComponents.CUSTOM_DATA) {
            noxesium$reloadComponents();
        }
    }
}
