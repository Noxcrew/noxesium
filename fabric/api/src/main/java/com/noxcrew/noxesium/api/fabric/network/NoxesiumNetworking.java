package com.noxcrew.noxesium.api.fabric.network;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Sets up the Noxesium networking system.
 */
public class NoxesiumNetworking {
    /**
     * The namespace under which all packets are registered.
     * Appended by a global API version equal to the major version of Noxesium.
     */
    public static final String PACKET_NAMESPACE = NoxesiumReferences.NAMESPACE + "-v3";

    /**
     * Whether to dump all incoming packets.
     */
    public static boolean dumpIncomingPackets = false;

    /**
     * Whether to dump all outgoing packets.
     */
    public static boolean dumpOutgoingPackets = false;

    /**
     * Checks if the connected server should be receiving packets of the given type.
     *
     * @param type The packet type
     * @return Whether the connected server should be receiving the packet
     */
    public static boolean canSend(NoxesiumPayloadType<?> type) {
        // Check if the server is willing to receive this packet and if we have registered this packet
        // on the client in the C2S registry!
        return ClientPlayNetworking.canSend(type.id())
                && ((PayloadTypeRegistryImpl<RegistryFriendlyByteBuf>) PayloadTypeRegistry.playC2S()).get(type.id())
                        != null;
    }
}
