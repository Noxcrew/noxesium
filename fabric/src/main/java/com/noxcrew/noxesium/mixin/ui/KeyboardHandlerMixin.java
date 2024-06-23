package com.noxcrew.noxesium.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.config.NoxesiumConfig;
import com.noxcrew.noxesium.feature.ui.wrapper.ElementManager;
import com.noxcrew.noxesium.feature.ui.wrapper.FpsOverlayWrapper;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.Util;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
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

    @WrapOperation(method = "handleDebugKeys", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"))
    public void addExperimentalPatchesHelpMessage(ChatComponent instance, Component component, Operation<Void> original) {
        if (component.getContents() instanceof TranslatableContents translatableContents) {
            if (translatableContents.getKey().equals("debug.pause.help")) {
                if (NoxesiumMod.getInstance().getConfig().hasConfiguredPerformancePatches()) {
                    instance.addMessage(Component.translatable("debug.experimental_patches.help"));
                }
            }
        }
        original.call(instance, component);
    }

    @ModifyReturnValue(method = "handleDebugKeys", at = @At("TAIL"))
    public boolean toggleExperimentalPatches(boolean original, int keyCode) {
        if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
            return original;
        }

        if (keyCode == InputConstants.KEY_W && NoxesiumMod.getInstance().getConfig().hasConfiguredPerformancePatches()) {
            if (Objects.equals(NoxesiumConfig.experimentalPatchesHotkey, false)) {
                NoxesiumConfig.experimentalPatchesHotkey = true;
                this.debugFeedbackTranslated("debug.experimental_patches.enabled");
            } else {
                NoxesiumConfig.experimentalPatchesHotkey = false;
                this.debugFeedbackTranslated("debug.experimental_patches.disabled");
            }

            // Update the fps overlay to show the Noxesium state
            ElementManager.getInstance(FpsOverlayWrapper.class).requestRedraw();
            return true;
        }
        return original;
    }
}
