package com.noxcrew.noxesium.mixin.ui.render;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.OverrideChunkUpdates;
import com.noxcrew.noxesium.feature.ui.wrapper.ElementManager;
import com.noxcrew.noxesium.feature.ui.wrapper.ChatWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.PrioritizeChunkUpdates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

/**
 * Syncs up with the server whenever relevant settings are updated.
 * Or whenever the chat spacing is changed we re-render chat.
 *
 * Also overrides the prioritize chunk updates setting while in HITW.
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
            ElementManager.getInstance(ChatWrapper.class).requestRedraw();
        }
        return original.call(instance);
    }

    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    private T overridePrioritizeChunkUpdates(T original) {
        var options = Minecraft.getInstance().options;
        if (((Object) (this)) == options.prioritizeChunkUpdates() && NoxesiumMod.getInstance().getModule(OverrideChunkUpdates.class).shouldOverride()) {
            return (T) PrioritizeChunkUpdates.NEARBY;
        }
        return original;
    }
}
