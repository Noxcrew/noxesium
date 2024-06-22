package com.noxcrew.noxesium.mixin.ui.render;

import com.noxcrew.noxesium.feature.ui.cache.ElementManager;
import com.noxcrew.noxesium.feature.ui.cache.ChatWrapper;
import net.minecraft.client.multiplayer.chat.ChatListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {

    @Inject(method = "clearQueue", at = @At("TAIL"))
    private void refreshChatOnClearQueue(CallbackInfo ci) {
        ElementManager.getInstance(ChatWrapper.class).requestRedraw();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void refreshChatOnTick(CallbackInfo ci) {
        ElementManager.getInstance(ChatWrapper.class).requestRedraw();
    }

    @Inject(method = "setMessageDelay", at = @At("TAIL"))
    private void refreshChatOnSetMessageDelay(CallbackInfo ci) {
        ElementManager.getInstance(ChatWrapper.class).requestRedraw();
    }

    @Inject(method = "acceptNextDelayedMessage", at = @At("TAIL"))
    private void refreshChatOnAcceptNextDelayedMessage(CallbackInfo ci) {
        ElementManager.getInstance(ChatWrapper.class).requestRedraw();
    }

    @Inject(method = "handleMessage", at = @At("TAIL"))
    private void refreshChatOnHandleMessage(CallbackInfo ci) {
        ElementManager.getInstance(ChatWrapper.class).requestRedraw();
    }
}
