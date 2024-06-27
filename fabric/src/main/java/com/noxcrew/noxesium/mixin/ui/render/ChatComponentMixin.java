package com.noxcrew.noxesium.mixin.ui.render;

import com.noxcrew.noxesium.feature.ui.wrapper.ElementManager;
import com.noxcrew.noxesium.feature.ui.wrapper.ChatWrapper;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @Inject(method = "scrollChat", at = @At("TAIL"))
    private void refreshChatOnScrollChat(CallbackInfo ci) {
        ElementManager.getInstance(ChatWrapper.class).requestRedraw();
    }

    @Inject(method = "resetChatScroll", at = @At("TAIL"))
    private void refreshChatOnResetChatScroll(CallbackInfo ci) {
        ElementManager.getInstance(ChatWrapper.class).requestRedraw();
    }

    @Inject(method = "refreshTrimmedMessages", at = @At("TAIL"))
    private void refreshChatOnRefreshTrimmedMessage(CallbackInfo ci) {
        ElementManager.getInstance(ChatWrapper.class).requestRedraw();
    }

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("TAIL"))
    private void refreshChatOnAddMessage(CallbackInfo ci) {
        ElementManager.getInstance(ChatWrapper.class).requestRedraw();
    }

    @Inject(method = "clearMessages", at = @At("TAIL"))
    private void refreshChatOnClearMessages(CallbackInfo ci) {
        ElementManager.getInstance(ChatWrapper.class).requestRedraw();
    }

    @Inject(method = "restoreState", at = @At("TAIL"))
    private void refreshChatOnRestoreState(CallbackInfo ci) {
        ElementManager.getInstance(ChatWrapper.class).requestRedraw();
    }
}
