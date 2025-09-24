package com.noxcrew.noxesium.api.nms.serialization;

import static com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry.registerSerializer;

import com.noxcrew.noxesium.api.network.NoxesiumErrorReason;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeCompletePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeTransferredPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundLazyPacketsPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundRegistryContentUpdatePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundRegistryIdsUpdatePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundLazyPacketsPacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundRegistryUpdateResultPacket;
import com.noxcrew.noxesium.api.nms.codec.NoxesiumStreamCodecs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
                ServerboundHandshakePacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
                        ServerboundHandshakePacket::entrypoints,
                        ServerboundHandshakePacket::new));
        registerSerializer(
                ServerboundHandshakeAcknowledgePacket.class,
                StreamCodec.composite(
                        NoxesiumStreamCodecs.ENTRYPOINT_PROTOCOL.apply(ByteBufCodecs.collection(HashSet::new)),
                        ServerboundHandshakeAcknowledgePacket::protocols,
                        NoxesiumStreamCodecs.MOD_INFO.apply(ByteBufCodecs.collection(HashSet::new)),
                        ServerboundHandshakeAcknowledgePacket::mods,
                        ServerboundHandshakeAcknowledgePacket::new));
        registerSerializer(
                ServerboundHandshakeCancelPacket.class,
                StreamCodec.composite(
                        NoxesiumStreamCodecs.forEnum(NoxesiumErrorReason.class),
                        ServerboundHandshakeCancelPacket::reason,
                        ServerboundHandshakeCancelPacket::new));
        registerSerializer(
                ServerboundRegistryUpdateResultPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ServerboundRegistryUpdateResultPacket::id,
                        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.VAR_INT),
                        ServerboundRegistryUpdateResultPacket::unknownKeys,
                        ServerboundRegistryUpdateResultPacket::new));
        registerSerializer(
                ServerboundLazyPacketsPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.collection(HashSet::new, NoxesiumStreamCodecs.KEY),
                        ServerboundLazyPacketsPacket::packets,
                        ServerboundLazyPacketsPacket::new));

        registerSerializer(
                ClientboundHandshakeAcknowledgePacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
                        ClientboundHandshakeAcknowledgePacket::entrypoints,
                        ClientboundHandshakeAcknowledgePacket::new));
        registerSerializer(
                ClientboundHandshakeCompletePacket.class, StreamCodec.unit(new ClientboundHandshakeCompletePacket()));
        registerSerializer(
                ClientboundHandshakeTransferredPacket.class,
                StreamCodec.unit(new ClientboundHandshakeTransferredPacket()));
        registerSerializer(
                ClientboundHandshakeCancelPacket.class,
                StreamCodec.composite(
                        NoxesiumStreamCodecs.forEnum(NoxesiumErrorReason.class),
                        ClientboundHandshakeCancelPacket::reason,
                        ClientboundHandshakeCancelPacket::new));
        registerSerializer(
                ClientboundRegistryIdsUpdatePacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ClientboundRegistryIdsUpdatePacket::id,
                        ByteBufCodecs.BOOL,
                        ClientboundRegistryIdsUpdatePacket::reset,
                        NoxesiumStreamCodecs.KEY,
                        ClientboundRegistryIdsUpdatePacket::registry,
                        ByteBufCodecs.map(HashMap::new, NoxesiumStreamCodecs.KEY, ByteBufCodecs.VAR_INT),
                        ClientboundRegistryIdsUpdatePacket::ids,
                        ClientboundRegistryIdsUpdatePacket::new));
        registerSerializer(
                ClientboundRegistryContentUpdatePacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ClientboundRegistryContentUpdatePacket::id,
                        ByteBufCodecs.BOOL,
                        ClientboundRegistryContentUpdatePacket::reset,
                        NoxesiumStreamCodecs.noxesiumRegistryPatch(),
                        ClientboundRegistryContentUpdatePacket::patch,
                        ClientboundRegistryContentUpdatePacket::new));
        registerSerializer(
                ClientboundLazyPacketsPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.collection(HashSet::new, NoxesiumStreamCodecs.KEY),
                        ClientboundLazyPacketsPacket::packets,
                        ClientboundLazyPacketsPacket::new));
    }
}
