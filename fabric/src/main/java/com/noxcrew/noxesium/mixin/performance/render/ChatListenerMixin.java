package com.noxcrew.noxesium.mixin.performance.render;

import com.noxcrew.noxesium.feature.render.cache.chat.ChatCache;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.ChatListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {

    @Inject(method = "clearQueue", at = @At("TAIL"))
    private void refreshChatOnClearQueue(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void refreshChatOnTick(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "setMessageDelay", at = @At("TAIL"))
    private void refreshChatOnSetMessageDelay(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "acceptNextDelayedMessage", at = @At("TAIL"))
    private void refreshChatOnAcceptNextDelayedMessage(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "handleMessage", at = @At("TAIL"))
    private void refreshChatOnHandleMessage(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }
}
