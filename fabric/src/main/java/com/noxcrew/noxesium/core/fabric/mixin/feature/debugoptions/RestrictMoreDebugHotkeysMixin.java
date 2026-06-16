package com.noxcrew.noxesium.core.fabric.mixin.feature.debugoptions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.core.feature.DebugOption;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adds various debug hotkeys for Noxesium.
 */
@Mixin(Gui.class)
public abstract class RestrictMoreDebugHotkeysMixin {

    @WrapOperation(
            method = "handleKeybinds",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;toggle()V"))
    public void preventHidingGui(Hud instance, Operation<Void> original) {
        var restrictedOptions =
                GameComponents.getInstance().noxesium$getComponent(CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS);
        if (restrictedOptions != null && restrictedOptions.contains(DebugOption.HIDE_UI.getKeyCode())) {
            instance.getChat()
                    .addClientSystemMessage(Component.translatable("debug.warning.option.disabled")
                            .withStyle(ChatFormatting.RED));
            return;
        }
        original.call(instance);
    }
}
