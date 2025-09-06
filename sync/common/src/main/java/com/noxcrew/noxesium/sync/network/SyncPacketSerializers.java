package com.noxcrew.noxesium.sync.network;

import static com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry.registerSerializer;

import com.noxcrew.noxesium.sync.network.clientbound.ClientboundRequestSyncEvent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Defines all common Noxesium sync serializers.
 */
public class SyncPacketSerializers {
    /**
     * Registers all serializers.
     */
    public static void register() {
        registerSerializer(
                SyncPackets.CLIENTBOUND_REQUEST_SYNC,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, ClientboundRequestSyncEvent::id, ClientboundRequestSyncEvent::new));
    }
}
