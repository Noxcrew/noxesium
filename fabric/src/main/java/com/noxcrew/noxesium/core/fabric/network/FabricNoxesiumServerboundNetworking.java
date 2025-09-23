package com.noxcrew.noxesium.core.fabric.network;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.network.ConnectionProtocolType;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.api.network.handshake.HandshakeState;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundLazyPacketsPacket;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import java.util.Set;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.kyori.adventure.key.Key;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Sets up networking for fabric in the serverbound direction.
 */
public class FabricNoxesiumServerboundNetworking extends NoxesiumServerboundNetworking {
    @Override
    public <T extends NoxesiumPacket> NoxesiumPayloadType<T> createPayloadType(
            @NotNull String namespace, @NotNull String id, Class<T> clazz, boolean clientToServer) {
        return new FabricPayloadType<>(Key.key(namespace, id), clazz, clientToServer);
    }

    @Override
    public @NotNull ConnectionProtocolType getMinecraftProtocol() {
        if (ClientNetworkingImpl.getClientPlayAddon() != null) {
            return ConnectionProtocolType.PLAY;
        }
        if (ClientNetworkingImpl.getClientConfigurationAddon() != null) {
            return ConnectionProtocolType.CONFIGURATION;
        }
        return ConnectionProtocolType.NONE;
    }

    @Override
    public boolean canSend(NoxesiumPayloadType<?> type) {
        // Check if the server is willing to receive this packet and if we have registered this packet
        // on the client in the registry! (the entrypoint is active)
        var resourceLocation = ResourceLocation.parse(type.id().asString());
        try {
            return getMinecraftProtocol() == ConnectionProtocolType.PLAY
                    && ClientPlayNetworking.canSend(resourceLocation)
                    && ((PayloadTypeRegistryImpl<RegistryFriendlyByteBuf>) PayloadTypeRegistry.playC2S())
                                    .get(resourceLocation)
                            != null;
        } catch (Exception e) {
            NoxesiumApi.getLogger().error("Failed to determine if packet of type '{}' can be sent", type);
            return false;
        }
    }

    @Override
    public <T extends NoxesiumPacket> boolean send(NoxesiumPayloadType<T> type, T payload) {
        // We assume the server indicates which packets it wishes to receive, otherwise we do not send anything.
        if (canSend(type)) {
            if (NoxesiumMod.getInstance().getConfig().dumpOutgoingPackets && Minecraft.getInstance().player != null) {
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
            if (type instanceof FabricPayloadType) {
                var fabricPayload = new NoxesiumPayload<>((FabricPayloadType<? super T>) type, payload);
                switch (getMinecraftProtocol()) {
                    case CONFIGURATION -> {
                        ClientConfigurationNetworking.send(fabricPayload);
                    }
                    case PLAY -> {
                        ClientPlayNetworking.send(fabricPayload);
                    }
                    default -> {
                        NoxesiumApi.getLogger().error("Cannot send payload when not in a configured protocol");
                    }
                }
            } else {
                throw new UnsupportedOperationException("Attempted to send payload of type '" + type.getClass()
                        + "' with Noxesium, which requires FabricPayloadType objects!");
            }
            return true;
        }
        return false;
    }

    @Override
    public void markLazyActive(NoxesiumPayloadType<?> payloadType) {
        // Directly inform the server that this packet has become active!
        // Ignore if we are not in the handshaking state.
        var handshaker = NoxesiumMod.getInstance().getHandshaker();
        if (handshaker == null || handshaker.getHandshakeState() == HandshakeState.NONE) return;
        send(new ServerboundLazyPacketsPacket(Set.of(payloadType.id())));
    }
}
