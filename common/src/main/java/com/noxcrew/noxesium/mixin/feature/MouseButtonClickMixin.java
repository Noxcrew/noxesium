package com.noxcrew.noxesium.mixin.feature;

import com.noxcrew.noxesium.network.serverbound.ServerboundMouseButtonClickPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseButtonClickMixin {

    @Inject(method = "onPress", at = @At("HEAD"))
    private void onPress(long p_91531_, int p_91532_, int p_91533_, int p_91534_, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        if (player == null) {
            return;
        }

        if (!client.gui.getChat().isChatFocused() && client.screen == null) {
            if (p_91533_ == 1) {
                if (p_91532_ == 0) {
                    player.displayClientMessage(Component.literal("Pressed Left click"), false);
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.PRESS_DOWN, ServerboundMouseButtonClickPacket.Button.LEFT).send();
                } else if (p_91532_ == 2) {
                    player.displayClientMessage(Component.literal("Pressed Middle click"), false);
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.PRESS_DOWN, ServerboundMouseButtonClickPacket.Button.MIDDLE).send();
                } else if (p_91532_ == 1) {
                    player.displayClientMessage(Component.literal("Pressed Right click"), false);
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.PRESS_DOWN, ServerboundMouseButtonClickPacket.Button.RIGHT).send();
                }
            } else if (p_91533_ == 0) {
                if (p_91532_ == 0) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.RELEASE, ServerboundMouseButtonClickPacket.Button.LEFT).send();
                } else if (p_91532_ == 2) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.RELEASE, ServerboundMouseButtonClickPacket.Button.MIDDLE).send();
                } else if (p_91532_ == 1) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.RELEASE, ServerboundMouseButtonClickPacket.Button.RIGHT).send();
                }
            }
        }
    }
}
