package com.noxcrew.noxesium.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import com.noxcrew.noxesium.api.util.DebugOption;
import com.noxcrew.noxesium.config.NoxesiumSettingsScreen;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @WrapOperation(
            method = "handleDebugKeys",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"))
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

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void interceptDebugKey(int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (ServerRules.RESTRICT_DEBUG_OPTIONS != null
                && ServerRules.RESTRICT_DEBUG_OPTIONS.getValue().contains(keyCode)) {
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

    @Redirect(
            method = "handleDebugKeys",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"))
    private void modifyAllHelpMessages(ChatComponent chatComponent, Component message) {
        String translationKey = noxesium$getTranslationKey(message);

        if (translationKey != null) {
            DebugOption debugOption = DebugOption.getByTranslationKey(translationKey);

            if (debugOption != null) {
                int keyCode = debugOption.getKeyCode();

                if (ServerRules.RESTRICT_DEBUG_OPTIONS != null
                        && ServerRules.RESTRICT_DEBUG_OPTIONS.getValue().contains(keyCode)) {
                    Component modifiedMessage = Component.translatable(translationKey)
                            .withStyle(style -> style.withStrikethrough(true)
                                    .withColor(0xFF9999)
                                    .withHoverEvent(new HoverEvent.ShowText(
                                            Component.translatable("debug.warning.option.disabled_by_server"))));
                    chatComponent.addMessage(modifiedMessage);
                    return;
                }
            }
        }

        chatComponent.addMessage(message);
    }

    @Inject(method = "handleChunkDebugKeys", at = @At("HEAD"), cancellable = true)
    private void onHandleChunkDebugKeys(int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (ServerRules.RESTRICT_DEBUG_OPTIONS != null
                && ServerRules.RESTRICT_DEBUG_OPTIONS.getValue().contains(keyCode)) {
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

    @Unique
    private String noxesium$getTranslationKey(Component component) {
        if (component instanceof MutableComponent && component.getContents() instanceof TranslatableContents) {
            return ((TranslatableContents) component.getContents()).getKey();
        }
        return null;
    }
}
