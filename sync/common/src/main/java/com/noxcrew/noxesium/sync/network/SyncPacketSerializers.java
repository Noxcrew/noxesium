package com.noxcrew.noxesium.sync.network;

import static com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry.registerSerializer;

import com.noxcrew.noxesium.sync.network.clientbound.ClientboundEstablishSyncPacket;
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundRequestSyncPacket;
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundSyncFilePacket;
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundRequestSyncPacket;
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundSyncFilePacket;
import java.util.HashMap;
import java.util.HashSet;
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
        var syncedPartCodec = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                SyncedPart::path,
                ByteBufCodecs.VAR_INT,
                SyncedPart::part,
                ByteBufCodecs.VAR_INT,
                SyncedPart::total,
                ByteBufCodecs.BYTE_ARRAY,
                SyncedPart::content,
                SyncedPart::new);

        registerSerializer(
                ClientboundRequestSyncPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundRequestSyncPacket::id,
                        ClientboundRequestSyncPacket::new));
        registerSerializer(
                ClientboundEstablishSyncPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ClientboundEstablishSyncPacket::syncId,
                        ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8),
                        ClientboundEstablishSyncPacket::requestedFiles,
                        ClientboundEstablishSyncPacket::new));
        registerSerializer(
                ClientboundSyncFilePacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ClientboundSyncFilePacket::syncId,
                        syncedPartCodec,
                        ClientboundSyncFilePacket::part,
                        ClientboundSyncFilePacket::new));
        registerSerializer(
                ServerboundRequestSyncPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        ServerboundRequestSyncPacket::id,
                        ByteBufCodecs.VAR_INT,
                        ServerboundRequestSyncPacket::syncId,
                        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_LONG),
                        ServerboundRequestSyncPacket::files,
                        ServerboundRequestSyncPacket::new));
        registerSerializer(
                ServerboundSyncFilePacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ServerboundSyncFilePacket::syncId,
                        syncedPartCodec,
                        ServerboundSyncFilePacket::part,
                        ServerboundSyncFilePacket::new));
    }
}
