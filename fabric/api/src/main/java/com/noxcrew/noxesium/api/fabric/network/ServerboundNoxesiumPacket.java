package com.noxcrew.noxesium.api.fabric.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/**
 * A Noxesium packet that is sent by the client and handled on the server.
 */
public interface ServerboundNoxesiumPacket extends NoxesiumPacket {
    /**
     * Sends this packet to the currently connected server, if possible. Returns whether the packet
     * was successfully sent or not.
     */
    default boolean send() {
        // We assume the server indicates which packets it wishes to receive, otherwise we do not send anything.
        if (NoxesiumNetworking.canSend(noxesiumType())) {
            if (NoxesiumNetworking.dumpOutgoingPackets) {
                Minecraft.getInstance()
                        .player
                        .displayClientMessage(
                                Component.empty()
                                        .append(Component.literal("[NOXESIUM] ")
                                                .withStyle(Style.EMPTY
                                                        .withBold(true)
                                                        .withColor(ChatFormatting.RED)))
                                        .append(Component.literal("[OUTGOING] ")
                                                .withStyle(Style.EMPTY
                                                        .withBold(true)
                                                        .withColor(ChatFormatting.AQUA)))
                                        .append(Component.literal(toString())
                                                .withStyle(Style.EMPTY
                                                        .withBold(false)
                                                        .withColor(ChatFormatting.WHITE))),
                                false);
            }
            ClientPlayNetworking.send(this);
            return true;
        }
        return false;
    }
}
