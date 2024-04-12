package com.noxcrew.noxesium.mixin.performance.render;

import com.noxcrew.noxesium.feature.render.cache.chat.ChatCache;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @Inject(method = "scrollChat", at = @At("TAIL"))
    private void refreshChatOnScrollChat(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "resetChatScroll", at = @At("TAIL"))
    private void refreshChatOnResetChatScroll(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "refreshTrimmedMessages", at = @At("TAIL"))
    private void refreshChatOnRefreshTrimmedMessage(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("TAIL"))
    private void refreshChatOnAddMessage(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "clearMessages", at = @At("TAIL"))
    private void refreshChatOnClearMessages(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "restoreState", at = @At("TAIL"))
    private void refreshChatOnRestoreState(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }
}
