package com.noxcrew.noxesium.mixin;

import com.noxcrew.noxesium.network.serverbound.ServerboundMouseButtonClickPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(MouseHandler.class)
public class MouseButtonClickMixin {

    @Unique
    private final List<Integer> noxesium$pressedButtons = new ArrayList<>();

    @Inject(method = "releaseMouse", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"))
    private void onReleaseMouse(CallbackInfo ci) {
        Iterator<Integer> iterator = noxesium$pressedButtons.iterator();
        while (iterator.hasNext()) {
            int button = iterator.next();
            if (button == 0) {
                new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.RELEASE, ServerboundMouseButtonClickPacket.Button.LEFT).send();
                iterator.remove();
            } else if (button == 2) {
                new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.RELEASE, ServerboundMouseButtonClickPacket.Button.MIDDLE).send();
                iterator.remove();
            } else if (button == 1) {
                new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.RELEASE, ServerboundMouseButtonClickPacket.Button.RIGHT).send();
                iterator.remove();
            }
        }
    }

    @Inject(method = "onPress", at = @At("HEAD"))
    private void onPress(long window, int button, int action, int mods, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        if (player == null) {
            return;
        }

        if (action == 1) {
            if (!client.gui.getChat().isChatFocused() && client.screen == null) {
                if (button == 0) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.PRESS_DOWN, ServerboundMouseButtonClickPacket.Button.LEFT).send();
                    noxesium$pressedButtons.add(button);
                } else if (button == 2) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.PRESS_DOWN, ServerboundMouseButtonClickPacket.Button.MIDDLE).send();
                    noxesium$pressedButtons.add(button);
                } else if (button == 1) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.PRESS_DOWN, ServerboundMouseButtonClickPacket.Button.RIGHT).send();
                    noxesium$pressedButtons.add(button);
                }
            }
        } else if (action == 0) {

            if (noxesium$pressedButtons.contains(button)) {
                if (button == 0) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.RELEASE, ServerboundMouseButtonClickPacket.Button.LEFT).send();
                    noxesium$pressedButtons.remove((Object) button);
                } else if (button == 2) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.RELEASE, ServerboundMouseButtonClickPacket.Button.MIDDLE).send();
                    noxesium$pressedButtons.remove((Object) button);
                } else if (button == 1) {
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.RELEASE, ServerboundMouseButtonClickPacket.Button.RIGHT).send();
                    noxesium$pressedButtons.remove((Object) button);
                }
            }
        }
    }
}
