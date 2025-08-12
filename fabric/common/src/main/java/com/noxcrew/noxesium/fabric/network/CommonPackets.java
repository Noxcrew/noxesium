package com.noxcrew.noxesium.fabric.network;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.fabric.network.PacketCollection;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundChangeServerRulesPacket;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundCustomSoundModifyPacket;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundCustomSoundStartPacket;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundCustomSoundStopPacket;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundOpenLinkPacket;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundResetExtraEntityDataPacket;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundResetPacket;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundResetServerRulesPacket;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundServerInformationPacket;
import com.noxcrew.noxesium.fabric.network.clientbound.ClientboundSetExtraEntityDataPacket;
import com.noxcrew.noxesium.fabric.network.serverbound.ServerboundClientSettingsPacket;
import com.noxcrew.noxesium.fabric.network.serverbound.ServerboundMouseButtonClickPacket;
import com.noxcrew.noxesium.fabric.network.serverbound.ServerboundQibTriggeredPacket;
import com.noxcrew.noxesium.fabric.network.serverbound.ServerboundRiptidePacket;

/**
 * Defines all common Noxesium packets.
 */
public class CommonPackets extends PacketCollection {
    /**
     * The instance of the common packet collection.
     */
    public static final CommonPackets INSTANCE = new CommonPackets();

    public final NoxesiumPayloadType<ServerboundClientSettingsPacket> SERVER_CLIENT_SETTINGS =
            server("serverbound_client_settings", ServerboundClientSettingsPacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ServerboundQibTriggeredPacket> SERVER_QIB_TRIGGERED =
            server("serverbound_qib_triggered", ServerboundQibTriggeredPacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ServerboundRiptidePacket> SERVER_RIPTIDE =
            server("serverbound_riptide", ServerboundRiptidePacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ServerboundMouseButtonClickPacket> SERVER_MOUSE_BUTTON_CLICK =
            server("serverbound_mouse_button_click", ServerboundMouseButtonClickPacket.STREAM_CODEC);

    public final NoxesiumPayloadType<ClientboundCustomSoundModifyPacket> CLIENT_CUSTOM_SOUND_MODIFY =
            client("clientbound_modify_sound", ClientboundCustomSoundModifyPacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ClientboundCustomSoundStartPacket> CLIENT_CUSTOM_SOUND_START =
            client("clientbound_start_sound", ClientboundCustomSoundStartPacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ClientboundCustomSoundStopPacket> CLIENT_CUSTOM_SOUND_STOP =
            client("clientbound_stop_sound", ClientboundCustomSoundStopPacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ClientboundChangeServerRulesPacket> CLIENT_CHANGE_SERVER_RULES =
            client("clientbound_change_server_rules", ClientboundChangeServerRulesPacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ClientboundResetServerRulesPacket> CLIENT_RESET_SERVER_RULES =
            client("clientbound_reset_server_rules", ClientboundResetServerRulesPacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ClientboundResetPacket> CLIENT_RESET =
            client("clientbound_reset", ClientboundResetPacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ClientboundServerInformationPacket> CLIENT_SERVER_INFO =
            client("clientbound_server_info", ClientboundServerInformationPacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ClientboundSetExtraEntityDataPacket> CLIENT_CHANGE_EXTRA_ENTITY_DATA =
            client("clientbound_change_extra_entity_data", ClientboundSetExtraEntityDataPacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ClientboundResetExtraEntityDataPacket> CLIENT_RESET_EXTRA_ENTITY_DATA =
            client("clientbound_reset_extra_entity_data", ClientboundResetExtraEntityDataPacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ClientboundOpenLinkPacket> CLIENT_OPEN_LINK =
            client("clientbound_open_link", ClientboundOpenLinkPacket.STREAM_CODEC);
}
