package com.noxcrew.noxesium.core.fabric.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.client.GuiElement;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatComponent.class)
public class ChatMixin {
    @WrapMethod(method = "render")
    public void wrapChatRender(GuiGraphics guiGraphics, int i, int j, int k, boolean bl, Operation<Void> original) {
        guiGraphics.pose().pushMatrix();
        var config = NoxesiumMod.getInstance().getConfig();
        guiGraphics.pose().scale((float) config.getScale(GuiElement.CHAT));
        original.call(guiGraphics, i, j, k, bl);
        guiGraphics.pose().popMatrix();
    }

    @WrapOperation(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;guiHeight()I"))
    public int wrapGetHeight(GuiGraphics instance, Operation<Integer> original) {
        return (int) (original.call(instance) / NoxesiumMod.getInstance().getConfig().getScale(GuiElement.CHAT));
    }
}
