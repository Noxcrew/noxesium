package com.noxcrew.noxesium.sync.network;

import static com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry.registerSerializer;

import com.noxcrew.noxesium.sync.network.clientbound.ClientboundRequestFilePacket;
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundRequestSyncPacket;
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundSyncFilePacket;
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundFileSystemPacket;
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundRequestSyncPacket;
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundSyncFilePacket;
import java.util.ArrayList;
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
                ByteBufCodecs.VAR_LONG,
                SyncedPart::modifyTime,
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
                ClientboundRequestFilePacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ClientboundRequestFilePacket::syncId,
                        ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8),
                        ClientboundRequestFilePacket::requestedFiles,
                        ClientboundRequestFilePacket::new));
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
                        ServerboundRequestSyncPacket::new));
        registerSerializer(
                ServerboundFileSystemPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ServerboundFileSystemPacket::syncId,
                        ByteBufCodecs.VAR_INT,
                        ServerboundFileSystemPacket::part,
                        ByteBufCodecs.VAR_INT,
                        ServerboundFileSystemPacket::total,
                        ByteBufCodecs.map(
                                HashMap::new,
                                ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                                ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_LONG)),
                        ServerboundFileSystemPacket::contents,
                        ServerboundFileSystemPacket::new));
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
