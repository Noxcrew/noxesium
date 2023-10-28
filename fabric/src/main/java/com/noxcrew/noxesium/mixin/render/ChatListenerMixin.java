package com.noxcrew.noxesium.mixin.render;

import com.noxcrew.noxesium.feature.render.cache.chat.ChatCache;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.ChatListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListener.class)
public class ChatListenerMixin {

    @Inject(method = "clearQueue", at = @At(value = "TAIL"))
    private void clearQueue(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void tick(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "setMessageDelay", at = @At(value = "TAIL"))
    private void setMessageDelay(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "acceptNextDelayedMessage", at = @At(value = "TAIL"))
    private void acceptNextDelayedMessage(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }

    @Inject(method = "handleMessage", at = @At(value = "TAIL"))
    private void handleMessage(CallbackInfo ci) {
        ChatCache.getInstance().clearCache();
    }
}
