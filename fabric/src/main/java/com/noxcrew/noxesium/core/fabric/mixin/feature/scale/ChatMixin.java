package com.noxcrew.noxesium.core.fabric.mixin.feature.scale;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.core.fabric.feature.ScalingExtension;
import com.noxcrew.noxesium.core.feature.GuiElement;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatComponent.class)
public class ChatMixin {
    @WrapMethod(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IIIZZ)V")
    public void wrapChatRender(
            GuiGraphics guiGraphics,
            Font font,
            int i,
            int j,
            int k,
            boolean bl,
            boolean bl2,
            Operation<Void> original) {
        ((ScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.CHAT, () -> {
            original.call(guiGraphics, font, i, j, k, bl, bl2);
        });
    }
}
