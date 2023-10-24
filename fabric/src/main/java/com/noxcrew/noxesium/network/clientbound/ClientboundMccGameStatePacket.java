package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Sent by MCC Island whenever the game state changes.
 * Possible values for the phase type:
 * - LOADING
 * - WAITING_FOR_PLAYERS
 * - PRE_GAME
 * - PLAY
 * - INTERMISSION
 * - POST_GAME
 * <p>
 * Values for the stage key differ based on the game.
 * <p>
 * Version differences:
 * 1 - Supports only the phase type and stage
 * 2 - Added rounds & map information
 * <p>
 * Clients should be able to parse any version of the packet so any server
 * can send any version.
 */
public class ClientboundMccGameStatePacket extends ClientboundNoxesiumPacket {

    public final String phaseType;
    public final String stage;

    /**
     * The current round of the game.
     */
    public final int round;

    /**
     * The total amount of rounds in the game.
     */
    public final int totalRounds;

    /**
     * The internal id assigned to the current map.
     */
    public final String mapId;

    /**
     * The display name of the current map localised to the client's language.
     */
    public final String mapName;

    public ClientboundMccGameStatePacket(FriendlyByteBuf buf, int version) {
        super(version);

        this.phaseType = buf.readUtf();
        this.stage = buf.readUtf();

        if (version >= 2) {
            this.round = buf.readVarInt();
            this.totalRounds = buf.readVarInt();
            this.mapId = buf.readUtf();
            this.mapName = buf.readUtf();
        } else {
            this.round = 0;
            this.totalRounds = 0;
            this.mapId = "";
            this.mapName = "";
        }
    }

    @Override
    public void receive(LocalPlayer player, PacketSender responseSender) {
        // This packet is sent to be handled by any other users of the MCC Island API.
        // TODO Set up a public packet handler API here?
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.CLIENT_MCC_GAME_STATE;
    }
}
