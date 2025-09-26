package com.noxcrew.noxesium.core.network;

import static com.noxcrew.noxesium.api.network.PacketCollection.client;
import static com.noxcrew.noxesium.api.network.PacketCollection.server;

import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadGroup;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundModifyPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundStartPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundStopPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundOpenLinkPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundOpenLinkV2Packet;
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

    public static final NoxesiumPayloadGroup SERVER_CLIENT_SETTINGS =
            server(INSTANCE, "serverbound_client_settings").add(ServerboundClientSettingsPacket.class);
    public static final NoxesiumPayloadGroup SERVER_QIB_TRIGGERED =
            server(INSTANCE, "serverbound_qib_triggered").add(ServerboundQibTriggeredPacket.class);
    public static final NoxesiumPayloadGroup SERVER_RIPTIDE =
            server(INSTANCE, "serverbound_riptide").add(ServerboundRiptidePacket.class);
    public static final NoxesiumPayloadGroup SERVER_MOUSE_BUTTON_CLICK =
            server(INSTANCE, "serverbound_mouse_button_click").markLazy().add(ServerboundMouseButtonClickPacket.class);

    public static final NoxesiumPayloadGroup CLIENT_CUSTOM_SOUND_MODIFY =
            client(INSTANCE, "clientbound_modify_sound").add(ClientboundCustomSoundModifyPacket.class);
    public static final NoxesiumPayloadGroup CLIENT_CUSTOM_SOUND_START =
            client(INSTANCE, "clientbound_start_sound").add(ClientboundCustomSoundStartPacket.class);
    public static final NoxesiumPayloadGroup CLIENT_CUSTOM_SOUND_STOP =
            client(INSTANCE, "clientbound_stop_sound").add(ClientboundCustomSoundStopPacket.class);
    public static final NoxesiumPayloadGroup CLIENT_UPDATE_ENTITY_COMPONENTS =
            client(INSTANCE, "clientbound_update_entity_components").add(ClientboundUpdateEntityComponentsPacket.class);
    public static final NoxesiumPayloadGroup CLIENT_UPDATE_GAME_COMPONENTS =
            client(INSTANCE, "clientbound_update_game_components").add(ClientboundUpdateGameComponentsPacket.class);
    public static final NoxesiumPayloadGroup CLIENT_OPEN_LINK = client(INSTANCE, "clientbound_open_link")
            .chain(ClientboundOpenLinkPacket.class)
            .add(
                    ClientboundOpenLinkV2Packet.class,
                    (it) -> new ClientboundOpenLinkPacket(it.text(), it.url()),
                    (it) -> new ClientboundOpenLinkV2Packet(it.text(), it.url(), ""))
            .group();
}
