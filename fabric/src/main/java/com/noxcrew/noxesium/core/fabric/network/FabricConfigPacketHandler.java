package com.noxcrew.noxesium.core.fabric.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A packet handler to be used during the configuration phase.
 */
public class FabricConfigPacketHandler<T extends CustomPacketPayload>
        implements ClientConfigurationNetworking.ConfigurationPayloadHandler<T> {

    @Override
    public void receive(T payload, ClientConfigurationNetworking.Context context) {
        if (payload instanceof NoxesiumPayload<?> noxesiumPayload) {
            if (noxesiumPayload.noxesiumType().hasListeners()) {
                noxesiumPayload
                        .noxesiumType()
                        .handle(context.client().getUser().getProfileId(), noxesiumPayload.value());
            }
        }
    }
}
