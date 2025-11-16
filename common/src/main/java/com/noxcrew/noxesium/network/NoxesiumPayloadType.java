package com.noxcrew.noxesium.network;

import com.noxcrew.noxesium.NoxesiumMod;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.tuple.Pair;

/**
 * A type of custom payload used by Noxesium for its packets.
 */
public class NoxesiumPayloadType<T extends NoxesiumPacket> {

    /**
     * The internal type of this payload.
     */
    public final CustomPacketPayload.Type<T> type;

    /**
     * All listeners registered to this payload type.
     */
    private final Set<Pair<WeakReference<?>, TriConsumer<?, T, PacketContext>>> listeners =
            ConcurrentHashMap.newKeySet();

    /**
     * Creates a new Noxesium payload type which can be listened to
     * by custom packet handlers.
     */
    public NoxesiumPayloadType(CustomPacketPayload.Type<T> type) {
        this.type = type;
    }

    /**
     * Returns the id of this payload type.
     */
    public ResourceLocation id() {
        return type.id();
    }

    /**
     * Handles a new packet [payload] of this type being received with
     * [context].
     */
    public void handle(PacketContext context, Object payload) {
        var iterator = listeners.iterator();
        while (iterator.hasNext()) {
            var pair = iterator.next();
            var obj = pair.getKey().get();
            if (obj == null) {
                iterator.remove();
                continue;
            }
            acceptAny(pair.getValue(), obj, context, payload);
        }

        if (NoxesiumMod.getInstance().getConfig().dumpIncomingPackets) {
            NoxesiumMod.getInstance().ensureMain(() -> {
                Minecraft.getInstance()
                        .player
                        .displayClientMessage(
                                Component.empty()
                                        .append(Component.literal("[NOXESIUM] ")
                                                .withStyle(Style.EMPTY
                                                        .withBold(true)
                                                        .withColor(ChatFormatting.RED)))
                                        .append(Component.literal("[INCOMING] ")
                                                .withStyle(Style.EMPTY
                                                        .withBold(true)
                                                        .withColor(ChatFormatting.YELLOW)))
                                        .append(Component.literal(payload.toString())
                                                .withStyle(Style.EMPTY
                                                        .withBold(false)
                                                        .withColor(ChatFormatting.WHITE))),
                                false);
            });
        }
    }

    /**
     * Registers a new listener to a packet payload of this type. Garbage collection
     * of this listener is tied to the lifetime of the reference. The reference object should
     * only ever be referenced from within the listener using the passed instance. This prevents
     * the listener from holding its own reference captive. If you do this the listener
     * will never be properly garbage collected.
     */
    public <R> void addListener(R reference, TriConsumer<R, T, PacketContext> listener) {
        listeners.removeIf((it) -> it.getKey().get() == null);
        listeners.add(Pair.of(new WeakReference<>(reference), listener));
    }

    /**
     * Casts [reference] to type [R] of [consumer].
     */
    private <R> void acceptAny(
            TriConsumer<R, T, PacketContext> consumer, Object reference, PacketContext context, Object payload) {
        consumer.accept((R) reference, (T) payload, context);
    }
}
