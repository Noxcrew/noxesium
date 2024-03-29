package com.noxcrew.noxesium.mixin.performance;

import com.mojang.blaze3d.platform.InputConstants;
import com.noxcrew.noxesium.NoxesiumConfig;
import com.noxcrew.noxesium.NoxesiumMod;
import net.minecraft.Util;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

/**
 * Adds various debug hotkeys for Noxesium.
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Shadow
    private long debugCrashKeyTime;

    @Shadow
    protected abstract void debugFeedbackTranslated(String string, Object... objects);

    @Redirect(method = "handleDebugKeys", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"))
    public void redirect(ChatComponent instance, Component component) {
        if (component.getContents() instanceof TranslatableContents translatableContents) {
            if (translatableContents.getKey().equals("debug.pause.help")) {
                if (NoxesiumMod.getInstance().getConfig().hasConfiguredPerformancePatches()) {
                    instance.addMessage(Component.translatable("debug.experimental_patches.help"));
                }
            }
        }
        instance.addMessage(component);
    }

    @Inject(method = "handleDebugKeys", at = @At("TAIL"), cancellable = true)
    public void injected(int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
            return;
        }

        if (keyCode == InputConstants.KEY_W && NoxesiumMod.getInstance().getConfig().hasConfiguredPerformancePatches()) {
            cir.setReturnValue(true);

            if (Objects.equals(NoxesiumConfig.experimentalPatchesHotkey, false)) {
                NoxesiumConfig.experimentalPatchesHotkey = true;
                this.debugFeedbackTranslated("debug.experimental_patches.enabled");
            } else {
                NoxesiumConfig.experimentalPatchesHotkey = false;
                this.debugFeedbackTranslated("debug.experimental_patches.disabled");
            }
        }
    }
}
