package com.noxcrew.noxesium.api.nms.network;

import static com.noxcrew.noxesium.api.nms.network.PacketCollection.client;
import static com.noxcrew.noxesium.api.nms.network.PacketCollection.server;

import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeCompletePacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundRegistryUpdatePacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakePacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundRegistryUpdateResultPacket;
import com.noxcrew.noxesium.api.nms.codec.NoxesiumStreamCodecs;
import com.noxcrew.noxesium.api.nms.network.payload.NoxesiumPayloadType;
import java.util.ArrayList;
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

    public static final NoxesiumPayloadType<ServerboundHandshakeCancelPacket> SERVERBOUND_HANDSHAKE_CANCEL = server(
            INSTANCE,
            "serverbound_handshake_cancel",
            ServerboundHandshakeCancelPacket.class,
            StreamCodec.unit(new ServerboundHandshakeCancelPacket()));

    public static final NoxesiumPayloadType<ServerboundRegistryUpdateResultPacket> SERVERBOUND_REGISTRY_UPDATE_RESULT =
            server(
                    INSTANCE,
                    "serverbound_registry_update_result",
                    ServerboundRegistryUpdateResultPacket.class,
                    StreamCodec.composite(
                            ByteBufCodecs.VAR_INT,
                            ServerboundRegistryUpdateResultPacket::id,
                            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.VAR_INT),
                            ServerboundRegistryUpdateResultPacket::unknownKeys,
                            ServerboundRegistryUpdateResultPacket::new));

    public static final NoxesiumPayloadType<ClientboundHandshakeAcknowledgePacket> CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE =
            client(
                    INSTANCE,
                    "clientbound_handshake_ack",
                    ClientboundHandshakeAcknowledgePacket.class,
                    StreamCodec.composite(
                            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
                            ClientboundHandshakeAcknowledgePacket::entrypoints,
                            ClientboundHandshakeAcknowledgePacket::new));

    public static final NoxesiumPayloadType<ClientboundRegistryUpdatePacket> CLIENTBOUND_REGISTRY_UPDATE = client(
            INSTANCE,
            "clientbound_registry_update",
            ClientboundRegistryUpdatePacket.class,
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ClientboundRegistryUpdatePacket::id,
                    NoxesiumStreamCodecs.KEY,
                    ClientboundRegistryUpdatePacket::registry,
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.VAR_INT, NoxesiumStreamCodecs.KEY),
                    ClientboundRegistryUpdatePacket::ids,
                    ClientboundRegistryUpdatePacket::new));

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
