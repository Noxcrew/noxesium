package com.noxcrew.noxesium.network;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * Implements a packet handler for Noxesium packets.
 */
public class NoxesiumPacketHandler<T extends NoxesiumPacket> implements IPayloadHandler<T> {

    @Override
    public void handle(T t, IPayloadContext iPayloadContext) {
        t.noxesiumType().handle(new PacketContext(Minecraft.getInstance(), Minecraft.getInstance().player), t);
    }
}
