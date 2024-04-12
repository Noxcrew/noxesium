package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.payload.NoxesiumPayloadType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Changes the stored value for one or more server rules.
 */
public record ClientboundChangeServerRulesPacket(IntList indices, List<Object> values) implements NoxesiumPacket {
    public static final StreamCodec<FriendlyByteBuf, ClientboundChangeServerRulesPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundChangeServerRulesPacket::write, ClientboundChangeServerRulesPacket::new);
    public static final NoxesiumPayloadType<ClientboundChangeServerRulesPacket> TYPE = NoxesiumPackets.client("change_server_rules", STREAM_CODEC);

    private ClientboundChangeServerRulesPacket(FriendlyByteBuf buf) {
        this(buf, buf.readIntIdList());
    }

    private ClientboundChangeServerRulesPacket(FriendlyByteBuf buf, IntList indices) {
        this(indices, readValues(buf, indices));
    }

    private static List<Object> readValues(FriendlyByteBuf buf, IntList indices) {
        var result = new ArrayList<>(indices.size());
        var module = NoxesiumMod.getInstance().getModule(ServerRuleModule.class);
        for (var index : indices) {
            // If we don't know one rule the whole packet is useless
            var rule = module.getIndex(index);
            if (rule == null) return result;
            var data = rule.read(buf);
            result.add(data);
        }
        return result;
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeIntIdList(indices);
        var module = NoxesiumMod.getInstance().getModule(ServerRuleModule.class);
        var idx = 0;
        for (var index : indices) {
            // If we don't know one rule the whole packet is useless
            var rule = module.getIndex(index);
            if (rule == null) throw new UnsupportedOperationException("Invalid server rule index " + index);
            var data = values.get(idx++);
            rule.writeUnsafe(data, buf);
        }
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return TYPE;
    }
}