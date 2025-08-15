package com.noxcrew.noxesium.core.fabric.network;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.nms.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.api.nms.network.payload.NoxesiumPayload;
import com.noxcrew.noxesium.api.nms.network.payload.NoxesiumPayloadType;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Sets up networking for fabric in the serverbound direction.
 */
public class FabricNoxesiumServerboundNetworking extends NoxesiumServerboundNetworking {
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, net.kyori.adventure.text.Component> getComponentStreamCodec() {
        return ComponentSerialization.STREAM_CODEC.map(
                NonWrappingComponentSerializer.INSTANCE::deserialize,
                NonWrappingComponentSerializer.INSTANCE::serialize);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ItemStack> getItemStackStreamCodec() {
        return ItemStack.OPTIONAL_STREAM_CODEC;
    }

    @Override
    public <T extends NoxesiumPacket> NoxesiumPayloadType<T> createPayloadType(
            @NotNull String namespace, @NotNull String id, StreamCodec<RegistryFriendlyByteBuf, T> codec, boolean clientToServer) {
        return new FabricNoxesiumPayloadType<>(
                ResourceLocation.fromNamespaceAndPath(namespace, id), codec, clientToServer);
    }

    @Override
    public boolean canSend(NoxesiumPayloadType<?> type) {
        // Check if the server is willing to receive this packet and if we have registered this packet
        // on the client in the C2S registry!
        return ClientPlayNetworking.canSend(type.id())
                && ((PayloadTypeRegistryImpl<RegistryFriendlyByteBuf>) PayloadTypeRegistry.playC2S()).get(type.id())
                        != null;
    }

    @Override
    public <T extends NoxesiumPacket> boolean send(NoxesiumPayloadType<T> type, T payload) {
        // We assume the server indicates which packets it wishes to receive, otherwise we do not send anything.
        if (canSend(type)) {
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
