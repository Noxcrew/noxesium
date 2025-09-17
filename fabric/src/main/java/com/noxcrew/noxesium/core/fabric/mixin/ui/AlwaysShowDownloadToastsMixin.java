package com.noxcrew.noxesium.core.fabric.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Makes toasts showing the download progress of a resource pack
 * always show while active!
 */
@Mixin(SystemToast.class)
public class AlwaysShowDownloadToastsMixin {
    @Shadow
    private Component title;

    @WrapOperation(
            method = "update",
            at =
                    @At(
                            value = "FIELD",
                            target =
                                    "Lnet/minecraft/client/gui/components/toasts/SystemToast;wantedVisibility:Lnet/minecraft/client/gui/components/toasts/Toast$Visibility;"))
    public void updateWantedVisibility(SystemToast instance, Toast.Visibility value, Operation<Void> original) {
        // Always show the download notifications!
        if (title.getContents() instanceof TranslatableContents translatableContents
                && (translatableContents.getKey().equals("download.pack.title")
                        || translatableContents.getKey().equals("download.pack.failed"))) {
            original.call(instance, Toast.Visibility.SHOW);
            return;
        }

        original.call(instance, value);
    }
}
