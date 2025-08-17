package com.noxcrew.noxesium.api.nms.network;

import static com.noxcrew.noxesium.api.nms.network.PacketCollection.client;
import static com.noxcrew.noxesium.api.nms.network.PacketCollection.server;

import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeCompletePacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundRegistryIdentifiersPacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakePacket;
import com.noxcrew.noxesium.api.nms.codec.NoxesiumStreamCodecs;
import com.noxcrew.noxesium.api.nms.network.payload.NoxesiumPayloadType;
import java.util.HashMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Defines the handshake packets.
 */
public class HandshakePackets {
    public static final PacketCollection INSTANCE = new PacketCollection();

    public static final NoxesiumPayloadType<ServerboundHandshakePacket> SERVERBOUND_HANDSHAKE = server(
            INSTANCE,
            "serverbound_handshake",
            ServerboundHandshakePacket.class,
            StreamCodec.composite(
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
                    ServerboundHandshakePacket::entrypoints,
                    ServerboundHandshakePacket::new));

    public static final NoxesiumPayloadType<ServerboundHandshakeAcknowledgePacket> SERVERBOUND_HANDSHAKE_ACKNOWLEDGE =
            server(
                    INSTANCE,
                    "serverbound_handshake_ack",
                    ServerboundHandshakeAcknowledgePacket.class,
                    StreamCodec.composite(
                            NoxesiumStreamCodecs.ENTRYPOINT_PROTOCOL.apply(ByteBufCodecs.list()),
                            ServerboundHandshakeAcknowledgePacket::protocols,
                            ServerboundHandshakeAcknowledgePacket::new));

    public static final NoxesiumPayloadType<ServerboundHandshakeCancelPacket> SERVERBOUND_HANDSHAKE_CANCEL = client(
            INSTANCE,
            "serverbound_handshake_cancel",
            ServerboundHandshakeCancelPacket.class,
            StreamCodec.unit(new ServerboundHandshakeCancelPacket()));

    public static final NoxesiumPayloadType<ClientboundHandshakeAcknowledgePacket> CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE =
            client(
                    INSTANCE,
                    "clientbound_handshake_ack",
                    ClientboundHandshakeAcknowledgePacket.class,
                    StreamCodec.composite(
                            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
                            ClientboundHandshakeAcknowledgePacket::entrypoints,
                            ClientboundHandshakeAcknowledgePacket::new));

    public static final NoxesiumPayloadType<ClientboundRegistryIdentifiersPacket> CLIENTBOUND_REGISTRY_IDS = client(
            INSTANCE,
            "clientbound_registry_ids",
            ClientboundRegistryIdentifiersPacket.class,
            StreamCodec.composite(
                    NoxesiumStreamCodecs.KEY,
                    ClientboundRegistryIdentifiersPacket::registry,
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.VAR_INT, NoxesiumStreamCodecs.KEY),
                    ClientboundRegistryIdentifiersPacket::ids,
                    ClientboundRegistryIdentifiersPacket::new));

    public static final NoxesiumPayloadType<ClientboundHandshakeCompletePacket> CLIENTBOUND_HANDSHAKE_COMPLETE = client(
            INSTANCE,
            "clientbound_handshake_complete",
            ClientboundHandshakeCompletePacket.class,
            StreamCodec.unit(new ClientboundHandshakeCompletePacket()));

    public static final NoxesiumPayloadType<ClientboundHandshakeCancelPacket> CLIENTBOUND_HANDSHAKE_CANCEL = client(
            INSTANCE,
            "clientbound_handshake_cancel",
            ClientboundHandshakeCancelPacket.class,
            StreamCodec.unit(new ClientboundHandshakeCancelPacket()));
}
