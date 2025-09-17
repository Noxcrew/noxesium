package com.noxcrew.noxesium.core.fabric.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.List;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;
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

    @Shadow
    private boolean forceHide;

    @Shadow
    private List<FormattedCharSequence> messageLines;

    @WrapOperation(
            method = "update",
            at =
                    @At(
                            value = "FIELD",
                            target =
                                    "Lnet/minecraft/client/gui/components/toasts/SystemToast;wantedVisibility:Lnet/minecraft/client/gui/components/toasts/Toast$Visibility;"))
    public void updateWantedVisibility(SystemToast instance, Toast.Visibility value, Operation<Void> original) {
        // Always show the download notifications! Ignore if there are no lines which can happen if
        // no actual download occurred so it never force hides.
        if (!forceHide
                && !messageLines.isEmpty()
                && title.getContents() instanceof TranslatableContents translatableContents
                && translatableContents.getKey().equals("download.pack.title")) {
            original.call(instance, Toast.Visibility.SHOW);
            return;
        }

        original.call(instance, value);
    }
}
