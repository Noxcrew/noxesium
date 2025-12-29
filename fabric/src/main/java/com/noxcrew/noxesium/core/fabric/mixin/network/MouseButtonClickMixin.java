package com.noxcrew.noxesium.core.fabric.mixin.network;

import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.network.CommonPackets;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundMouseButtonClickPacket;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseButtonClickMixin {

    @Unique
    private final List<ServerboundMouseButtonClickPacket.Button> noxesium$pressedButtons = new ArrayList<>();

    @Inject(
            method = "releaseMouse",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(Lcom/mojang/blaze3d/platform/Window;IDD)V"))
    private void onReleaseMouse(CallbackInfo ci) {
        if (!NoxesiumServerboundNetworking.getInstance().shouldSendLazy(CommonPackets.SERVER_MOUSE_BUTTON_CLICK))
            return;
        var iterator = noxesium$pressedButtons.iterator();
        while (iterator.hasNext()) {
            var button = iterator.next();
            NoxesiumServerboundNetworking.send(
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.RELEASE, button));
            iterator.remove();
        }
    }

    @Inject(method = "onButton", at = @At("HEAD"))
    private void onButton(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        var client = Minecraft.getInstance();
        var player = client.player;
        if (player == null) return;
        if (!NoxesiumServerboundNetworking.getInstance().shouldSendLazy(CommonPackets.SERVER_MOUSE_BUTTON_CLICK))
            return;

        // Check that the action and button is valid
        if (action != 0 && action != 1) return;
        var buttonId = info.button();
        if (buttonId < 0 || buttonId > 2) return;

        // Determine which button this id refers to
        var button =
                switch (buttonId) {
                    case 0 -> ServerboundMouseButtonClickPacket.Button.LEFT;
                    case 1 -> ServerboundMouseButtonClickPacket.Button.RIGHT;
                    case 2 -> ServerboundMouseButtonClickPacket.Button.MIDDLE;
                    default -> throw new IllegalStateException("Unexpected value: " + buttonId);
                };

        if (action == 1) {
            if (!client.gui.getChat().isChatFocused() && client.screen == null) {
                // Only check for one press per tick to avoid spam!
                if (NoxesiumMod.getInstance().sentButtonClicks.contains(button)) return;
                NoxesiumMod.getInstance().sentButtonClicks.add(button);

                NoxesiumServerboundNetworking.send(new ServerboundMouseButtonClickPacket(
                        ServerboundMouseButtonClickPacket.Action.PRESS_DOWN, button));
                noxesium$pressedButtons.add(button);
            }
        } else if (noxesium$pressedButtons.contains(button)) {
            NoxesiumServerboundNetworking.send(
                    new ServerboundMouseButtonClickPacket(ServerboundMouseButtonClickPacket.Action.RELEASE, button));
            noxesium$pressedButtons.remove(button);
        }
    }
}
