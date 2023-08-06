package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Changes the stored value for one or more server rules.
 */
public class ClientboundChangeServerRulesPacket extends ClientboundNoxesiumPacket {

    private final IntList indices;
    private final List<Object> values;

    public ClientboundChangeServerRulesPacket(FriendlyByteBuf buf) {
        super(buf.readVarInt());
        this.indices = buf.readIntIdList();
        this.values = new ArrayList<>(indices.size());

        for (var index : indices) {
            var rule = ServerRuleModule.getInstance().getIndex(index);

            // If we don't know one rule the whole packet is useless
            if (rule == null) return;
            var data = rule.read(buf);
            this.values.add(data);
        }
    }

    @Override
    public void receive(LocalPlayer player, PacketSender responseSender) {
        for (var idx = 0; idx < indices.size(); idx++) {
            var index = indices.getInt(idx);
            var rule = ServerRuleModule.getInstance().getIndex(index);
            if (rule == null) return;
            rule.setUnsafe(values.get(idx));
        }
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.CLIENT_CHANGE_SERVER_RULES;
    }
}
