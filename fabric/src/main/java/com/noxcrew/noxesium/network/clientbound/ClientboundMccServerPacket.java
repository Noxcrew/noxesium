package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.island.MccIslandTracker;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Sent by MCC Island whenever you switch servers. All values are dynamic and may change over time.
 */
public class ClientboundMccServerPacket extends ClientboundNoxesiumPacket {

    public final String type;
    public final String subType;
    public final String associatedGame;

    /**
     * The legacy id of this game, these are only used by mcc.live.
     *
     * These are provided for completeness and comparison, but you should not ever
     * depend on these as future games will not receive one.
     */
    public final String legacyGameId;

    public ClientboundMccServerPacket(FriendlyByteBuf buf) {
        super(buf.readVarInt());
        this.type = buf.readUtf();
        this.subType = buf.readUtf();
        this.associatedGame = buf.readUtf();
        this.legacyGameId = buf.readUtf();
    }

    @Override
    public void receive(LocalPlayer player, PacketSender responseSender) {
        // This packet is sent to be handled by any other users of the MCC Island API.
        // TODO Set up a public packet handler API here?

        // Mark down that the player is on MCC Island (we assume no other server sends these)
        NoxesiumMod.getInstance().getModule(MccIslandTracker.class).markOnMccIsland();
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.CLIENT_MCC_SERVER;
    }
}
