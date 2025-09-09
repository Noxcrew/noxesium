package com.noxcrew.noxesium.core.network;

import static com.noxcrew.noxesium.api.network.PacketCollection.client;
import static com.noxcrew.noxesium.api.network.PacketCollection.server;

import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundModifyPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundStartPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundStopPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundOpenLinkPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundUpdateBlockEntityComponentsPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundUpdateEntityComponentsPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundUpdateGameComponentsPacket;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundClientSettingsPacket;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundMouseButtonClickPacket;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundQibTriggeredPacket;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundRiptidePacket;

/**
 * Defines all common Noxesium packets.
 */
public class CommonPackets {
    public static final PacketCollection INSTANCE = new PacketCollection();

    public static final NoxesiumPayloadType<ServerboundClientSettingsPacket> SERVER_CLIENT_SETTINGS =
            server(INSTANCE, "serverbound_client_settings", ServerboundClientSettingsPacket.class);
    public static final NoxesiumPayloadType<ServerboundQibTriggeredPacket> SERVER_QIB_TRIGGERED =
            server(INSTANCE, "serverbound_qib_triggered", ServerboundQibTriggeredPacket.class);
    public static final NoxesiumPayloadType<ServerboundRiptidePacket> SERVER_RIPTIDE =
            server(INSTANCE, "serverbound_riptide", ServerboundRiptidePacket.class);
    public static final NoxesiumPayloadType<ServerboundMouseButtonClickPacket> SERVER_MOUSE_BUTTON_CLICK =
            server(INSTANCE, "serverbound_mouse_button_click", ServerboundMouseButtonClickPacket.class);

    public static final NoxesiumPayloadType<ClientboundCustomSoundModifyPacket> CLIENT_CUSTOM_SOUND_MODIFY =
            client(INSTANCE, "clientbound_modify_sound", ClientboundCustomSoundModifyPacket.class);
    public static final NoxesiumPayloadType<ClientboundCustomSoundStartPacket> CLIENT_CUSTOM_SOUND_START =
            client(INSTANCE, "clientbound_start_sound", ClientboundCustomSoundStartPacket.class);
    public static final NoxesiumPayloadType<ClientboundCustomSoundStopPacket> CLIENT_CUSTOM_SOUND_STOP =
            client(INSTANCE, "clientbound_stop_sound", ClientboundCustomSoundStopPacket.class);
    public static final NoxesiumPayloadType<ClientboundUpdateEntityComponentsPacket> CLIENT_UPDATE_ENTITY_COMPONENTS =
            client(INSTANCE, "clientbound_update_entity_components", ClientboundUpdateEntityComponentsPacket.class);
    public static final NoxesiumPayloadType<ClientboundUpdateBlockEntityComponentsPacket>
            CLIENT_UPDATE_BLOCK_ENTITY_COMPONENTS = client(
                    INSTANCE,
                    "clientbound_update_block_entity_components",
                    ClientboundUpdateBlockEntityComponentsPacket.class);
    public static final NoxesiumPayloadType<ClientboundUpdateGameComponentsPacket> CLIENT_UPDATE_GAME_COMPONENTS =
            client(INSTANCE, "clientbound_update_game_components", ClientboundUpdateGameComponentsPacket.class);
    public static final NoxesiumPayloadType<ClientboundOpenLinkPacket> CLIENT_OPEN_LINK =
            client(INSTANCE, "clientbound_open_link", ClientboundOpenLinkPacket.class);
}
