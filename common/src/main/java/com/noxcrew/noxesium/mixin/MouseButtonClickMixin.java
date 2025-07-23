package com.noxcrew.noxesium.mixin;

import com.noxcrew.noxesium.network.serverbound.ServerboundMouseButtonClickPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseButtonClickMixin {

    @Inject(method = "onPress", at = @At("HEAD"))
    private void onPress(long window, int button, int action, int mods, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        if (player == null) {
            return;
        }

        if (!client.gui.getChat().isChatFocused() && client.screen == null) {
            if (action == 1) {
                if (button == 0) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.PRESS_DOWN, ServerboundMouseButtonClickPacket.Button.LEFT).send();
                } else if (button == 2) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.PRESS_DOWN, ServerboundMouseButtonClickPacket.Button.MIDDLE).send();
                } else if (button == 1) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.PRESS_DOWN, ServerboundMouseButtonClickPacket.Button.RIGHT).send();
                }
            } else if (action == 0) {
                if (button == 0) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.RELEASE, ServerboundMouseButtonClickPacket.Button.LEFT).send();
                } else if (button == 2) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.RELEASE, ServerboundMouseButtonClickPacket.Button.MIDDLE).send();
                } else if (button == 1) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.RELEASE, ServerboundMouseButtonClickPacket.Button.RIGHT).send();
                }
            }
        }
    }
}
