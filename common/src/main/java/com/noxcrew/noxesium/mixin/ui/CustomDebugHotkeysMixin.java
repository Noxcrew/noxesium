package com.noxcrew.noxesium.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import com.noxcrew.noxesium.config.NoxesiumSettingsScreen;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds various debug hotkeys for Noxesium.
 */
@Mixin(KeyboardHandler.class)
public abstract class CustomDebugHotkeysMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    private long debugCrashKeyTime;

    @ModifyReturnValue(method = "handleDebugKeys", at = @At("TAIL"))
    public boolean openSettingsMenu(boolean original, @Local(argsOnly = true) KeyEvent event) {
        if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
            return original;
        }
        if (event.key() == InputConstants.KEY_W) {
            Minecraft.getInstance().setScreen(new NoxesiumSettingsScreen(null));
            return true;
        }
        return original;
    }

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void interceptDebugKey(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (ServerRules.RESTRICT_DEBUG_OPTIONS != null
                && ServerRules.RESTRICT_DEBUG_OPTIONS.getValue().contains(event.key())) {
            if (minecraft != null) {
                minecraft
                        .gui
                        .getChat()
                        .addMessage(Component.translatable("debug.warning.option.disabled")
                                .withStyle(ChatFormatting.RED));
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "handleChunkDebugKeys", at = @At("HEAD"), cancellable = true)
    private void onHandleChunkDebugKeys(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (ServerRules.RESTRICT_DEBUG_OPTIONS != null
                && ServerRules.RESTRICT_DEBUG_OPTIONS.getValue().contains(event.key())) {
            if (minecraft != null) {
                minecraft
                        .gui
                        .getChat()
                        .addMessage(Component.translatable("debug.warning.option.disabled")
                                .withStyle(ChatFormatting.RED));
            }
            cir.setReturnValue(true);
        }
    }
}
