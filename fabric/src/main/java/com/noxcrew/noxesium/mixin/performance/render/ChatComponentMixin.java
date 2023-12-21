package com.noxcrew.noxesium.mixin.performance.render;

import com.noxcrew.noxesium.feature.render.cache.chat.ChatCache;
import com.noxcrew.noxesium.feature.render.cache.tablist.TabListCache;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(method = "refreshTrimmedMessage", at = @At("TAIL"))
    private void refreshChatOnRefreshTrimmedMessage(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V", at = @At("TAIL"))
    private void refreshChatOnAddMessage(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "clearMessages", at = @At("TAIL"))
    private void refreshChatOnClearMessages(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }
}
