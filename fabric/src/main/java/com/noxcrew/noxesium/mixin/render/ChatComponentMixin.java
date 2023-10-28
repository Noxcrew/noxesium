package com.noxcrew.noxesium.mixin.render;

import com.noxcrew.noxesium.feature.render.cache.chat.ChatCache;
import com.noxcrew.noxesium.feature.render.cache.tablist.TabListCache;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @Inject(method = "scrollChat", at = @At(value = "TAIL"))
    private void scrollChat(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "resetChatScroll", at = @At(value = "TAIL"))
    private void resetChatScroll(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "refreshTrimmedMessage", at = @At(value = "TAIL"))
    private void refreshTrimmedMessage(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V", at = @At(value = "TAIL"))
    private void addMessage(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "clearMessages", at = @At(value = "TAIL"))
    private void clearMessages(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }
}
