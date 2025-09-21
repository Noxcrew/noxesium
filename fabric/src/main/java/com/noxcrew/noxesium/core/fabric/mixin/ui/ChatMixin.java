package com.noxcrew.noxesium.core.fabric.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.api.client.GuiElement;
import com.noxcrew.noxesium.core.fabric.feature.render.GuiGraphicsScalingExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatComponent.class)
public class ChatMixin {
    @WrapMethod(method = "render")
    public void wrapChatRender(GuiGraphics guiGraphics, int i, int j, int k, boolean bl, Operation<Void> original) {
        ((GuiGraphicsScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.CHAT, () -> {
            original.call(guiGraphics, i, j, k, bl);
        });
    }
}
