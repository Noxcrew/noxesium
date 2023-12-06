package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.NoxesiumMod;
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
public class ClientboundResetServerRulesPacket extends ClientboundNoxesiumPacket {

    private final IntList indices;

    public ClientboundResetServerRulesPacket(FriendlyByteBuf buf) {
        super(buf.readVarInt());
        this.indices = buf.readIntIdList();
    }

    @Override
    public void receive(LocalPlayer player, PacketSender responseSender) {
        var module = NoxesiumMod.getInstance().getModule(ServerRuleModule.class);
        for (var index : indices) {
            var rule = module.getIndex(index);
            if (rule == null) continue;
            rule.reset();
        }
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.CLIENT_RESET_SERVER_RULES;
    }
}
