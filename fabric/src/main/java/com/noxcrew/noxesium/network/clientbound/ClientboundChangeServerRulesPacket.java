package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.rule.RuleIndexProvider;
import com.noxcrew.noxesium.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
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

    private ClientboundChangeServerRulesPacket(FriendlyByteBuf buf) {
        this(buf, buf.readIntIdList());
    }

    private ClientboundChangeServerRulesPacket(FriendlyByteBuf buf, IntList indices) {
        this(indices, readValues(NoxesiumMod.getInstance().getModule(ServerRuleModule.class), buf, indices));
    }

    /**
     * Reads a set of rule values from a buffer.
     */
    public static List<Object> readValues(RuleIndexProvider provider, FriendlyByteBuf buf, IntList indices) {
        var result = new ArrayList<>(indices.size());
        for (var index : indices) {
            // If we don't know one rule the whole packet is useless
            var rule = provider.getIndex(index);
            if (rule == null) return result;
            var data = rule.read(buf);
            result.add(data);
        }
        return result;
    }

    /**
     * Writes a set of rule values to a buffer.
     */
    public static void write(RuleIndexProvider provider, FriendlyByteBuf buf, IntList indices, List<Object> values) {
        buf.writeIntIdList(indices);
        var idx = 0;
        for (var index : indices) {
            // If we don't know one rule the whole packet is useless
            var rule = provider.getIndex(index);
            if (rule == null) throw new UnsupportedOperationException("Invalid rule index " + index);
            var data = values.get(idx++);
            rule.writeUnsafe(data, buf);
        }
    }

    private void write(FriendlyByteBuf buf) {
        write(NoxesiumMod.getInstance().getModule(ServerRuleModule.class), buf, indices, values);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return NoxesiumPackets.CHANGE_SERVER_RULES;
    }
}