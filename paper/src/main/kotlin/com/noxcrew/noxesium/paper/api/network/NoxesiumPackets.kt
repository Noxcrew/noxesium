package com.noxcrew.noxesium.paper.api.network

import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundChangeServerRulesPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundCustomSoundModifyPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundCustomSoundStartPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundCustomSoundStopPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundMccGameStatePacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundMccServerPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundResetExtraEntityDataPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundResetPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundResetServerRulesPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundServerInformationPacket
import com.noxcrew.noxesium.paper.api.network.serverbound.ServerboundClientInformationPacket
import com.noxcrew.noxesium.paper.api.network.serverbound.ServerboundClientSettingsPacket
import com.noxcrew.noxesium.paper.api.network.serverbound.ServerboundNoxesiumPacket
import net.minecraft.network.FriendlyByteBuf
import org.bukkit.entity.Player

/** Stores and registers all known Noxesium packets. */
public object NoxesiumPackets {

    private val _clientboundPackets = mutableMapOf<String, PacketType<*>>()
    private val _serverboundPackets = mutableMapOf<String, ServerboundPacketType<*>>()

    public val SERVER_CLIENT_INFO: ServerboundPacketType<ServerboundClientInformationPacket> = server("client_info", ::ServerboundClientInformationPacket)
    public val SERVER_CLIENT_SETTINGS: ServerboundPacketType<ServerboundClientSettingsPacket> = server("client_settings", ::ServerboundClientSettingsPacket)

    public val CLIENT_CHANGE_SERVER_RULES: PacketType<ClientboundChangeServerRulesPacket> = client("change_server_rules")
    public val CLIENT_RESET_SERVER_RULES: PacketType<ClientboundResetServerRulesPacket> = client("reset_server_rules")
    public val CLIENT_RESET: PacketType<ClientboundResetPacket> = client("reset")
    public val CLIENT_SERVER_INFO: PacketType<ClientboundServerInformationPacket> = client("server_info")

    public val CLIENT_MCC_SERVER: PacketType<ClientboundMccServerPacket> = client("mcc_server")
    public val CLIENT_MCC_GAME_STATE: PacketType<ClientboundMccGameStatePacket> = client("mcc_game_state")

    public val CLIENT_START_SOUND: PacketType<ClientboundCustomSoundStartPacket> = client("start_sound")
    public val CLIENT_MODIFY_SOUND: PacketType<ClientboundCustomSoundModifyPacket> = client("modify_sound")
    public val CLIENT_STOP_SOUND: PacketType<ClientboundCustomSoundStopPacket> = client("stop_sound")

    public val CLIENT_CHANGE_EXTRA_ENTITY_DATA: PacketType<ClientboundCustomSoundStopPacket> = client("change_extra_entity_data")
    public val CLIENT_RESET_EXTRA_ENTITY_DATA: PacketType<ClientboundResetExtraEntityDataPacket> = client("reset_extra_entity_data")

    /** All registered client-bound packets. */
    public val clientboundPackets: Map<String, PacketType<*>>
        get() = _clientboundPackets

    /** All registered server-bound packets. */
    public val serverboundPackets: Map<String, ServerboundPacketType<*>>
        get() = _serverboundPackets

    /** Registers a client-bound packet. */
    public fun <T : NoxesiumPacket> client(id: String): PacketType<T> {
        require(id !in _clientboundPackets) { "Cannot register clientbound packet $id twice!" }
        val packetType = PacketType<T>(id)
        _clientboundPackets[id] = packetType
        return packetType
    }

    /** Registers a server-bound packet. */
    public fun <T : ServerboundNoxesiumPacket> server(id: String, reader: (FriendlyByteBuf, Player, Int) -> T): ServerboundPacketType<T> {
        require(id !in _serverboundPackets) { "Cannot register serverbound packet $id twice!" }
        val packetType = ServerboundPacketType(id, reader)
        _serverboundPackets[id] = packetType
        return packetType
    }
}
