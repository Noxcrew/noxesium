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
 */
public class ClientboundMccGameStatePacket extends ClientboundNoxesiumPacket {

    public final String phaseType;
    public final String stage;

    public ClientboundMccGameStatePacket(FriendlyByteBuf buf) {
        super(buf.readVarInt());
        this.phaseType = buf.readUtf();
        this.stage = buf.readUtf();
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
