package com.noxcrew.noxesium.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A simple packet handler used by Noxesium which defers the result of the packet
 * to the packet instance itself.
 */
public class NoxesiumPacketHandler<T extends CustomPacketPayload>
        implements ClientPlayNetworking.PlayPayloadHandler<T> {

    @Override
    public void receive(T payload, ClientPlayNetworking.Context context) {
        if (payload instanceof NoxesiumPacket noxesiumPacket) {
            noxesiumPacket.noxesiumType().handle(new PacketContext(context.client(), context.player()), payload);
        }
    }
}
