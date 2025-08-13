package com.noxcrew.noxesium.fabric.mcc;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

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
 * Clients should be able to parse any version of the packet so any server
 * can send any version.
 *
 * @param phaseType   The current type of phase.
 * @param stage       The current stage id.
 * @param round       The current round of the game.
 * @param totalRounds The total amount of rounds in the game.
 * @param mapId       The internal id assigned to the current map.
 * @param mapName     The display name of the current map localised to the client's language.
 */
public record ClientboundMccGameStatePacket(
        String phaseType, String stage, int round, int totalRounds, String mapId, String mapName)
        implements NoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundMccGameStatePacket> STREAM_CODEC =
            CustomPacketPayload.codec(ClientboundMccGameStatePacket::write, ClientboundMccGameStatePacket::new);

    private ClientboundMccGameStatePacket(RegistryFriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(), buf.readUtf());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(phaseType);
        buf.writeUtf(stage);
        buf.writeVarInt(round);
        buf.writeVarInt(totalRounds);
        buf.writeUtf(mapId);
        buf.writeUtf(mapName);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return MccPackets.INSTANCE.CLIENT_MCC_GAME_STATE;
    }
}
