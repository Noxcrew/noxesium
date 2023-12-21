package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.feature.skull.SkullFontModule;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundModule;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

import static com.noxcrew.noxesium.api.util.ByteUtil.hasFlag;

/**
 * Sent by the server to reset one or more features of the client.
 * The flags byte has the following results:
 * 0x01 - Resets all server rule values
 * 0x02 - Resets cached player heads
 */
public class ClientboundResetPacket extends ClientboundNoxesiumPacket {

    private final byte flags;

    public ClientboundResetPacket(FriendlyByteBuf buf) {
        super(buf.readVarInt());
        this.flags = buf.readByte();
    }

    @Override
    public void receive(LocalPlayer player, PacketSender responseSender) {
        if (hasFlag(flags, 0)) {
            NoxesiumMod.getInstance().getModule(ServerRuleModule.class).clearAll();
        }
        if (hasFlag(flags, 1)) {
            NoxesiumMod.getInstance().getModule(SkullFontModule.class).resetCaches();
        }
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.CLIENT_RESET;
    }
}
