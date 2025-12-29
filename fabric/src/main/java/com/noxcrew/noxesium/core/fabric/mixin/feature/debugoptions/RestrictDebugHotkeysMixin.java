package com.noxcrew.noxesium.core.fabric.mixin.feature.debugoptions;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import com.noxcrew.noxesium.core.feature.DebugOption;
import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.core.fabric.config.NoxesiumSettingsScreen;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
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
public abstract class RestrictDebugHotkeysMixin {

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
        var restrictedOptions =
                GameComponents.getInstance().noxesium$getComponent(CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS);
        if (restrictedOptions != null && restrictedOptions.contains(event.key())) {
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
        var restrictedOptions =
                GameComponents.getInstance().noxesium$getComponent(CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS);
        if (restrictedOptions != null && restrictedOptions.contains(event.key())) {
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

    @WrapOperation(
            method = "keyPress",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z", ordinal = 1))
    public void preventHidingGui(Options instance, boolean value, Operation<Void> original) {
        var restrictedOptions =
                GameComponents.getInstance().noxesium$getComponent(CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS);
        if (restrictedOptions != null && restrictedOptions.contains(DebugOption.HIDE_UI.getKeyCode())) {
            if (minecraft != null) {
                minecraft
                        .gui
                        .getChat()
                        .addMessage(Component.translatable("debug.warning.option.disabled")
                                .withStyle(ChatFormatting.RED));
            }
            return;
        }
        original.call(instance, value);
    }
}
