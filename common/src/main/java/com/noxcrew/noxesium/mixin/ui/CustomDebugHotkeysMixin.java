package com.noxcrew.noxesium.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import com.noxcrew.noxesium.config.NoxesiumSettingsScreen;
import net.minecraft.Util;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adds various debug hotkeys for Noxesium.
 */
@Mixin(KeyboardHandler.class)
public abstract class CustomDebugHotkeysMixin {

    @Shadow
    private long debugCrashKeyTime;

    @WrapOperation(method = "handleDebugKeys", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"))
    public void extendHelpMessage(ChatComponent instance, Component component, Operation<Void> original) {
        if (component.getContents() instanceof TranslatableContents translatableContents) {
            if (translatableContents.getKey().equals("debug.pause.help")) {
                instance.addMessage(Component.translatable("debug.noxesium_settings.help"));
            }
        }
        original.call(instance, component);
    }

    @ModifyReturnValue(method = "handleDebugKeys", at = @At("TAIL"))
    public boolean openSettingsMenu(boolean original, int keyCode) {
        if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
            return original;
        }
        if (keyCode == InputConstants.KEY_V) {
            Minecraft.getInstance().setScreen(new NoxesiumSettingsScreen(null));
            return true;
        }
        return original;
    }
}
