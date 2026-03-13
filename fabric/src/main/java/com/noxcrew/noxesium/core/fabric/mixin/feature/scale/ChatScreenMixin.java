package com.noxcrew.noxesium.core.fabric.mixin.feature.scale;

import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.feature.GuiElement;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @ModifyArg(
            method = "mouseClicked",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/ActiveTextCollector$ClickableStyleFinder;<init>(Lnet/minecraft/client/gui/Font;II)V"),
            index = 1)
    public int onMousePositionX(int original) {
        return (int) (original / NoxesiumMod.getInstance().getConfig().getScale(GuiElement.CHAT));
    }

    @ModifyArg(
            method = "mouseClicked",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/ActiveTextCollector$ClickableStyleFinder;<init>(Lnet/minecraft/client/gui/Font;II)V"),
            index = 2)
    public int onMousePositionY(int original) {
        return (int) (original / NoxesiumMod.getInstance().getConfig().getScale(GuiElement.CHAT));
    }

    @ModifyArg(
            method = "mouseClicked",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/ChatComponent;captureClickableText(Lnet/minecraft/client/gui/ActiveTextCollector;IIZ)V"),
            index = 1)
    public int onScreenHeight(int original) {
        return (int) (original / NoxesiumMod.getInstance().getConfig().getScale(GuiElement.CHAT));
    }
}
