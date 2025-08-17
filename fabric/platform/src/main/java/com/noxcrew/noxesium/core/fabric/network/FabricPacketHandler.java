package com.noxcrew.noxesium.core.fabric.network;

import com.noxcrew.noxesium.api.nms.network.payload.NoxesiumPayload;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A simple packet handler used by Noxesium which defers the result of the packet
 * to the packet instance itself.
 */
public class FabricPacketHandler<T extends CustomPacketPayload> implements ClientPlayNetworking.PlayPayloadHandler<T> {

    @Override
    public void receive(T payload, ClientPlayNetworking.Context context) {
        if (payload instanceof NoxesiumPayload<?> noxesiumPayload) {
            if (NoxesiumMod.getInstance().getConfig().dumpIncomingPackets) {
                Minecraft.getInstance()
                    .player
                    .displayClientMessage(
                        Component.empty()
                            .append(Component.literal("[NOXESIUM] ")
                                .withStyle(Style.EMPTY
                                    .withBold(true)
                                    .withColor(ChatFormatting.RED)))
                            .append(Component.literal("[INCOMING] ")
                                .withStyle(Style.EMPTY
                                    .withBold(true)
                                    .withColor(ChatFormatting.YELLOW)))
                            .append(Component.literal(
                                    noxesiumPayload.value().toString())
                                .withStyle(Style.EMPTY
                                    .withBold(false)
                                    .withColor(ChatFormatting.WHITE))),
                        false);
            }
            if (noxesiumPayload.noxesiumType().hasListeners()) {
                noxesiumPayload.noxesiumType().handle(context.player(), noxesiumPayload.value());
            }
        }
    }
}
