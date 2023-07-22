package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.feature.rule.ServerRule;
import com.noxcrew.noxesium.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Resets the stored value for one or more server rules.
 */
public class ClientboundChangeServerRulesPacket extends ClientboundNoxesiumPacket {

    private final IntList indices;
    private final FriendlyByteBuf buffer;

    public ClientboundChangeServerRulesPacket(FriendlyByteBuf buf) {
        super(buf.readVarInt());
        this.indices = buf.readIntIdList();
        this.buffer = buf;
    }

    @Override
    public void receive(LocalPlayer player, PacketSender responseSender) {
        for (var index : indices) {
            var rule = ServerRuleModule.getInstance().getIndex(index);
            if (rule == null) continue;

            // TODO Can we do something that does not involve passing along the buffer?
            rule.set(buffer);
        }
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.CLIENT_CHANGE_SERVER_RULES;
    }
}
