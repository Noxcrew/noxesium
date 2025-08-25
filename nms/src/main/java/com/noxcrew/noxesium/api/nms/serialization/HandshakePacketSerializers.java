package com.noxcrew.noxesium.api.nms.serialization;

import static com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry.registerSerializer;

import com.noxcrew.noxesium.api.network.handshake.HandshakePackets;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeCompletePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundRegistryUpdatePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundRegistryUpdateResultPacket;
import com.noxcrew.noxesium.api.nms.codec.NoxesiumStreamCodecs;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Defines the handshake packet serializers.
 */
public class HandshakePacketSerializers {
    /**
     * Registers all serializers.
     */
    public static void register() {
        registerSerializer(
                HandshakePackets.SERVERBOUND_HANDSHAKE,
                StreamCodec.composite(
                        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
                        ServerboundHandshakePacket::entrypoints,
                        ServerboundHandshakePacket::new));
        registerSerializer(
                HandshakePackets.SERVERBOUND_HANDSHAKE_ACKNOWLEDGE,
                StreamCodec.composite(
                        NoxesiumStreamCodecs.ENTRYPOINT_PROTOCOL.apply(ByteBufCodecs.list()),
                        ServerboundHandshakeAcknowledgePacket::protocols,
                        ServerboundHandshakeAcknowledgePacket::new));
        registerSerializer(
                HandshakePackets.SERVERBOUND_HANDSHAKE_CANCEL,
                StreamCodec.unit(new ServerboundHandshakeCancelPacket()));
        registerSerializer(
                HandshakePackets.SERVERBOUND_REGISTRY_UPDATE_RESULT,
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ServerboundRegistryUpdateResultPacket::id,
                        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.VAR_INT),
                        ServerboundRegistryUpdateResultPacket::unknownKeys,
                        ServerboundRegistryUpdateResultPacket::new));

        registerSerializer(
                HandshakePackets.CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE,
                StreamCodec.composite(
                        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
                        ClientboundHandshakeAcknowledgePacket::entrypoints,
                        ClientboundHandshakeAcknowledgePacket::new));
        registerSerializer(
                HandshakePackets.CLIENTBOUND_REGISTRY_UPDATE,
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ClientboundRegistryUpdatePacket::id,
                        NoxesiumStreamCodecs.KEY,
                        ClientboundRegistryUpdatePacket::registry,
                        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.VAR_INT, NoxesiumStreamCodecs.KEY),
                        ClientboundRegistryUpdatePacket::ids,
                        ClientboundRegistryUpdatePacket::new));
        registerSerializer(
                HandshakePackets.CLIENTBOUND_HANDSHAKE_COMPLETE,
                StreamCodec.unit(new ClientboundHandshakeCompletePacket()));
        registerSerializer(
                HandshakePackets.CLIENTBOUND_HANDSHAKE_CANCEL,
                StreamCodec.unit(new ClientboundHandshakeCancelPacket()));
    }
}
