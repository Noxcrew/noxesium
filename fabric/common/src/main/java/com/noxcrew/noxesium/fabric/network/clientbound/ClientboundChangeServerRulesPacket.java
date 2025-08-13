package com.noxcrew.noxesium.fabric.network.clientbound;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.fabric.NoxesiumMod;
import com.noxcrew.noxesium.fabric.feature.rule.RuleIndexProvider;
import com.noxcrew.noxesium.fabric.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.fabric.network.CommonPackets;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Changes the stored value for one or more server rules.
 */
public record ClientboundChangeServerRulesPacket(IntList indices, List<Object> values) implements NoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundChangeServerRulesPacket> STREAM_CODEC =
            CustomPacketPayload.codec(
                    ClientboundChangeServerRulesPacket::write, ClientboundChangeServerRulesPacket::new);

    private ClientboundChangeServerRulesPacket(RegistryFriendlyByteBuf buf) {
        this(buf, buf.readIntIdList());
    }

    private ClientboundChangeServerRulesPacket(RegistryFriendlyByteBuf buf, IntList indices) {
        this(indices, readValues(NoxesiumMod.getInstance().getFeature(ServerRuleModule.class), buf, indices));
    }

    /**
     * Reads a set of rule values from a buffer.
     */
    public static List<Object> readValues(RuleIndexProvider provider, RegistryFriendlyByteBuf buf, IntList indices) {
        var result = new ArrayList<>(indices.size());
        for (var index : indices) {
            // If we don't know one rule the whole packet is useless
            var rule = provider.getIndex(index);
            if (rule == null) throw new UnsupportedOperationException("Invalid rule index " + index);
            var data = rule.read(buf);
            result.add(data);
        }
        return result;
    }

    /**
     * Writes a set of rule values to a buffer.
     */
    public static void write(
            RuleIndexProvider provider, RegistryFriendlyByteBuf buf, IntList indices, List<Object> values) {
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

    private void write(RegistryFriendlyByteBuf buf) {
        write(NoxesiumMod.getInstance().getFeature(ServerRuleModule.class), buf, indices, values);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return CommonPackets.INSTANCE.CLIENT_CHANGE_SERVER_RULES;
    }
}
