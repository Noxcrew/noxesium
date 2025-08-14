package com.noxcrew.noxesium.api.fabric.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.fabric.network.payload.NoxesiumPayload;
import com.noxcrew.noxesium.api.fabric.network.payload.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/**
 * Sets up the Noxesium networking system.
 */
public class NoxesiumNetworking {
    private static final Map<Class<?>, NoxesiumPayloadType<?>> packetTypes = new HashMap<>();

    /**
     * Registers a new payload type.
     */
    public static void register(NoxesiumPayloadType<?> payloadType) {
        var clazz = payloadType.getClass();
        Preconditions.checkState(!packetTypes.containsKey(clazz), "Cannot register payload type '" + clazz + "' twice");
        packetTypes.put(clazz, payloadType);
    }

    /**
     * Unregisters a payload type.
     */
    public static void unregister(NoxesiumPayloadType<?> payloadType) {
        packetTypes.remove(payloadType.getClass());
    }

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

    /**
     * Sends the given packet, automatically detects the type of the packet based on the registered packets.
     */
    public static boolean send(NoxesiumPacket packet) {
        var type = packetTypes.get(packet.getClass());
        if (type == null) return false;
        return type.sendAny(packet);
    }

    /**
     * Sends this packet to the currently connected server, if possible. Returns whether the packet
     * was successfully sent or not.
     */
    public static <T extends NoxesiumPacket> boolean send(NoxesiumPayloadType<T> type, T payload) {
        // We assume the server indicates which packets it wishes to receive, otherwise we do not send anything.
        if (NoxesiumNetworking.canSend(type)) {
            if (NoxesiumMod.getInstance().getConfig().dumpOutgoingPackets) {
                Minecraft.getInstance()
                        .player
                        .displayClientMessage(
                                Component.empty()
                                        .append(Component.literal("[NOXESIUM] ")
                                                .withStyle(Style.EMPTY
                                                        .withBold(true)
                                                        .withColor(ChatFormatting.RED)))
                                        .append(Component.literal("[OUTGOING] ")
                                                .withStyle(Style.EMPTY
                                                        .withBold(true)
                                                        .withColor(ChatFormatting.AQUA)))
                                        .append(Component.literal(payload.toString())
                                                .withStyle(Style.EMPTY
                                                        .withBold(false)
                                                        .withColor(ChatFormatting.WHITE))),
                                false);
            }
            ClientPlayNetworking.send(new NoxesiumPayload<>(type, payload));
            return true;
        }
        return false;
    }
}
