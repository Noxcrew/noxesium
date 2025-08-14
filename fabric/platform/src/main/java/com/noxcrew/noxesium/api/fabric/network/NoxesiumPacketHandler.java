package com.noxcrew.noxesium.api.fabric.network;

import com.noxcrew.noxesium.api.fabric.network.payload.NoxesiumPayload;
import com.noxcrew.noxesium.api.fabric.network.payload.PacketContext;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A simple packet handler used by Noxesium which defers the result of the packet
 * to the packet instance itself.
 */
public class NoxesiumPacketHandler<T extends CustomPacketPayload>
        implements ClientPlayNetworking.PlayPayloadHandler<T> {

    @Override
    public void receive(T payload, ClientPlayNetworking.Context context) {
        if (payload instanceof NoxesiumPayload<?> noxesiumPacket) {
            if (noxesiumPacket.noxesiumType().hasListeners()) {
                noxesiumPacket.noxesiumType().handle(new PacketContext(context.client(), context.player()), payload);
            }
            if (NoxesiumMod.getInstance().getConfig().dumpIncomingPackets) {
                Minecraft.getInstance()
                        .player
                        .displayClientMessage(
                                Component.empty()
                                        .append(Component.literal("[NOXESIUM] ")
                                                .withStyle(Style.EMPTY
                                                        .withBold(true)
                                                        .withColor(ChatFormatting.RED)))
                                        .append(Component.literal("[INCOMING] ")
                                                .withStyle(Style.EMPTY
                                                        .withBold(true)
                                                        .withColor(ChatFormatting.YELLOW)))
                                        .append(Component.literal(payload.toString())
                                                .withStyle(Style.EMPTY
                                                        .withBold(false)
                                                        .withColor(ChatFormatting.WHITE))),
                                false);
            }
        }
    }
}
