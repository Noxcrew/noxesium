package com.noxcrew.noxesium.fabric.network;

import static com.noxcrew.noxesium.api.fabric.network.PacketCollection.client;
import static com.noxcrew.noxesium.api.fabric.network.PacketCollection.server;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.fabric.network.PacketCollection;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundCustomSoundModifyPacket;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundCustomSoundStartPacket;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundCustomSoundStopPacket;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundOpenLinkPacket;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundUpdateComponentsPacket;
import com.noxcrew.noxesium.fabric.network.serverbound.ServerboundClientSettingsPacket;
import com.noxcrew.noxesium.fabric.network.serverbound.ServerboundMouseButtonClickPacket;
import com.noxcrew.noxesium.fabric.network.serverbound.ServerboundQibTriggeredPacket;
import com.noxcrew.noxesium.fabric.network.serverbound.ServerboundRiptidePacket;

/**
 * Defines all common Noxesium packets.
 */
public class CommonPackets {
    public static final PacketCollection INSTANCE = new PacketCollection();

    public static final NoxesiumPayloadType<ServerboundClientSettingsPacket> SERVER_CLIENT_SETTINGS =
            server(INSTANCE, "serverbound_client_settings", ServerboundClientSettingsPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ServerboundQibTriggeredPacket> SERVER_QIB_TRIGGERED =
            server(INSTANCE, "serverbound_qib_triggered", ServerboundQibTriggeredPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ServerboundRiptidePacket> SERVER_RIPTIDE =
            server(INSTANCE, "serverbound_riptide", ServerboundRiptidePacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ServerboundMouseButtonClickPacket> SERVER_MOUSE_BUTTON_CLICK =
            server(INSTANCE, "serverbound_mouse_button_click", ServerboundMouseButtonClickPacket.STREAM_CODEC);

    public static final NoxesiumPayloadType<ClientboundCustomSoundModifyPacket> CLIENT_CUSTOM_SOUND_MODIFY =
            client(INSTANCE, "clientbound_modify_sound", ClientboundCustomSoundModifyPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundCustomSoundStartPacket> CLIENT_CUSTOM_SOUND_START =
            client(INSTANCE, "clientbound_start_sound", ClientboundCustomSoundStartPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundCustomSoundStopPacket> CLIENT_CUSTOM_SOUND_STOP =
            client(INSTANCE, "clientbound_stop_sound", ClientboundCustomSoundStopPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundUpdateComponentsPacket> CLIENT_UPDATE_COMPONENTS =
            client(INSTANCE, "clientbound_update_components", ClientboundUpdateComponentsPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundOpenLinkPacket> CLIENT_OPEN_LINK =
            client(INSTANCE, "clientbound_open_link", ClientboundOpenLinkPacket.STREAM_CODEC);
}
