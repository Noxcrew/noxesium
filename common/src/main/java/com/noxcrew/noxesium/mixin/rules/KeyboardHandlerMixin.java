package com.noxcrew.noxesium.mixin.rules;

import com.noxcrew.noxesium.feature.dkeys.DebugKey;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyboardHandler;
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

import net.minecraft.client.Minecraft;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void onHandleDebugKeys(int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (ServerRules.RESTRICT_DEBUG_OPTIONS != null && ServerRules.RESTRICT_DEBUG_OPTIONS.getValue().contains(keyCode)) {
            if (minecraft != null) {
                minecraft.gui.getChat().addMessage(
                        Component.translatable("noxesium.warning.debug_option.disabled")
                                .withStyle(ChatFormatting.RED)
                );
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "handleChunkDebugKeys", at = @At("HEAD"), cancellable = true)
    private void onHandleChunkDebugKeys(int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (ServerRules.RESTRICT_DEBUG_OPTIONS != null && ServerRules.RESTRICT_DEBUG_OPTIONS.getValue().contains(keyCode)) {
            if (minecraft != null) {
                minecraft.gui.getChat().addMessage(
                        Component.translatable("noxesium.warning.debug_option.disabled")
                                .withStyle(ChatFormatting.RED)
                );
            }
            cir.setReturnValue(true);
        }
    }

    @Redirect(method = "handleDebugKeys",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"))
    private void modifyAllHelpMessages(ChatComponent chatComponent, Component message) {
        String translationKey = noxesium$getTranslationKey(message);

        if (translationKey != null) {
            DebugKey debugKey = DebugKey.getByTranslationKey(translationKey);

            if (debugKey != null) {
                int keyCode = debugKey.getKeyCode();

                if (ServerRules.RESTRICT_DEBUG_OPTIONS != null && ServerRules.RESTRICT_DEBUG_OPTIONS.getValue().contains(keyCode)) {
                    Component modifiedMessage = Component.translatable(translationKey)
                            .withStyle(style -> style
                                    .withStrikethrough(true)
                                    .withColor(0xFF9999)
                                    .withHoverEvent(new HoverEvent.ShowText(Component.translatable("noxesium.warning.debug_option.disabled_by_server"))));
                    chatComponent.addMessage(modifiedMessage);
                    return;
                }
            }
        }

        chatComponent.addMessage(message);
    }

    @Unique
    private String noxesium$getTranslationKey(Component component) {
        if (component instanceof MutableComponent && component.getContents() instanceof TranslatableContents) {
            return ((TranslatableContents) component.getContents()).getKey();
        }
        return null;
    }
}