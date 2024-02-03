package com.noxcrew.noxesium.mixin.performance.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.render.cache.chat.ChatCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

/**
 * Syncs up with the server whenever relevant settings are updated.
 * Or whenever the chat spacing is changed we re-render chat.
 */
@Mixin(OptionInstance.class)
public abstract class OptionInstanceMixin<T> {

    @WrapOperation(method = "set", at = @At(value = "FIELD", target = "Lnet/minecraft/client/OptionInstance;onValueUpdate:Ljava/util/function/Consumer;"))
    private Consumer<T> updateNoxesiumOptions(OptionInstance<T> instance, Operation<Consumer<T>> original) {
        var options = Minecraft.getInstance().options;
        if (instance == options.touchscreen() ||
                instance == options.notificationDisplayTime()) {
            NoxesiumMod.syncGuiScale();
        }
        if (instance == options.chatLineSpacing()) {
            ChatCache.getInstance().clearCache();
        }
        return original.call(instance);
    }
}
