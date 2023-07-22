package com.noxcrew.noxesium.network;

import com.noxcrew.noxesium.network.clientbound.ClientboundNoxesiumPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.player.LocalPlayer;

/**
 * A simple packet handler used by Noxesium which defers the result of the packet
 * to the packet instance itself.
 */
public class NoxesiumPacketHandler implements ClientPlayNetworking.PlayPacketHandler<FabricPacket> {

    @Override
    public void receive(FabricPacket packet, LocalPlayer player, PacketSender responseSender) {
        if (packet instanceof ClientboundNoxesiumPacket clientboundNoxesiumPacket) {
            clientboundNoxesiumPacket.receive(player, responseSender);
        }
    }
}
