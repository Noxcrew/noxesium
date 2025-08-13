package com.noxcrew.noxesium.core.fabric.mixin.rules;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.fabric.feature.misc.SyncGuiScale;
import com.noxcrew.noxesium.core.fabric.registry.CommonGameComponentTypes;
import java.util.function.Consumer;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.PrioritizeChunkUpdates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Syncs up with the server whenever relevant settings are updated.
 * Or whenever the chat spacing is changed we re-render chat.
 * <p>
 * Also overrides the prioritize chunk updates setting while in HITW or the
 * graphics mode setting based on the server rule.
 */
@Mixin(OptionInstance.class)
public abstract class OptionInstanceMixin<T> {

    @WrapOperation(
            method = "set",
            at =
                    @At(
                            value = "FIELD",
                            target =
                                    "Lnet/minecraft/client/OptionInstance;onValueUpdate:Ljava/util/function/Consumer;"))
    private Consumer<T> updateNoxesiumOptions(OptionInstance<T> instance, Operation<Consumer<T>> original) {
        var options = Minecraft.getInstance().options;
        if (instance == options.touchscreen() || instance == options.notificationDisplayTime()) {
            NoxesiumApi.getInstance().getFeatureOptional(SyncGuiScale.class).ifPresent(SyncGuiScale::syncGuiScale);
        }
        return original.call(instance);
    }

    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    private T overridePrioritizeChunkUpdates(T original) {
        var options = Minecraft.getInstance().options;
        if (options == null) return original;

        // Ignore if we're in a nested settings menu override
        if (NoxesiumMod.getInstance().disableSettingOverrides) return original;

        if (((Object) (this)) == options.prioritizeChunkUpdates()
                && Minecraft.getInstance()
                        .noxesium$hasComponent(CommonGameComponentTypes.DISABLE_DEFERRED_CHUNK_UPDATES)) {
            return (T) PrioritizeChunkUpdates.NEARBY;
        }
        if (((Object) (this)) == options.graphicsMode()) {
            var graphics =
                    Minecraft.getInstance().noxesium$getComponent(CommonGameComponentTypes.OVERRIDE_GRAPHICS_MODE);
            if (graphics != null && (!NoxesiumMod.getInstance().isUsingIris || graphics != GraphicsStatus.FABULOUS)) {
                return (T) graphics;
            }
        }
        return original;
    }
}
