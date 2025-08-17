package com.noxcrew.noxesium.api.nms.network.payload;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.nms.network.NoxesiumClientboundNetworking;
import com.noxcrew.noxesium.api.nms.network.NoxesiumNetworking;
import com.noxcrew.noxesium.api.nms.network.NoxesiumServerboundNetworking;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A type of custom payload used by Noxesium for its packets.
 */
public class NoxesiumPayloadType<T extends NoxesiumPacket> {
    /**
     * The internal type of this payload.
     */
    public final CustomPacketPayload.Type<NoxesiumPayload<T>> type;

    /**
     * The codec of this type.
     */
    public final StreamCodec<RegistryFriendlyByteBuf, T> codec;

    /**
     * The class of the packet type.
     */
    public final Class<T> clazz;

    /**
     * Whether this payload is sent from client to server.
     */
    public final boolean clientToServer;

    /**
     * All listeners registered to this payload type.
     */
    private final Set<Pair<WeakReference<?>, TriConsumer<?, T, Player>>> listeners = ConcurrentHashMap.newKeySet();

    /**
     * Creates a new Noxesium payload type which can be listened to
     * by custom packet handlers.
     */
    public NoxesiumPayloadType(
            @NotNull ResourceLocation key,
            @NotNull StreamCodec<RegistryFriendlyByteBuf, T> codec,
            @NotNull Class<T> clazz,
            boolean clientToServer) {
        this.type = new CustomPacketPayload.Type<>(key);
        this.codec = codec;
        this.clazz = clazz;
        this.clientToServer = clientToServer;
    }

    /**
     * Returns the id of this payload type.
     */
    public ResourceLocation id() {
        return type.id();
    }

    /**
     * Returns the class of the packet payload type.
     */
    public Class<T> typeClass() {
        return clazz;
    }

    /**
     * Returns a stream codec for this payload.
     */
    public StreamCodec<RegistryFriendlyByteBuf, NoxesiumPayload<T>> getStreamCodec() {
        var payloadType = this;
        return new StreamCodec<>() {
            @Override
            @NotNull
            public NoxesiumPayload<T> decode(RegistryFriendlyByteBuf buffer) {
                return new NoxesiumPayload<>(payloadType, codec.decode(buffer));
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, NoxesiumPayload<T> payload) {
                codec.encode(buffer, payload.value());
            }
        };
    }

    /**
     * Registers a receiver for this payload type.
     */
    public void register(@Nullable NoxesiumEntrypoint entrypoint) {
        NoxesiumNetworking.getInstance().register(this, entrypoint);
    }

    /**
     * Unregisters the receiver for this payload type.
     */
    public void unregister() {
        NoxesiumNetworking.getInstance().unregister(this);
    }

    /**
     * Returns whether this type has listeners.
     */
    public boolean hasListeners() {
        return !listeners.isEmpty();
    }

    /**
     * Handles a new packet [payload] of this type being received with
     * [context].
     */
    public void handle(@NotNull Player context, @NotNull Object payload) {
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
    }

    /**
     * Registers a new listener to a packet payload of this type. Garbage collection
     * of this listener is tied to the lifetime of the reference. The reference object should
     * only ever be referenced from within the listener using the passed instance. This prevents
     * the listener from holding its own reference captive. If you do this the listener
     * will never be properly garbage collected.
     */
    public <R> void addListener(R reference, @NotNull TriConsumer<R, T, Player> listener) {
        listeners.removeIf((it) -> it.getKey().get() == null);
        listeners.add(Pair.of(new WeakReference<>(reference), listener));
    }

    /**
     * Sends the given [payload] as the type of this payload.
     */
    public boolean sendServerboundAny(Object payload) {
        return NoxesiumServerboundNetworking.getInstance().send(this, (T) payload);
    }

    /**
     * Sends the given [payload] as the type of this payload.
     */
    public boolean sendClientboundAny(Player player, Object payload) {
        return NoxesiumClientboundNetworking.getInstance().send(player, this, (T) payload);
    }

    /**
     * Casts [reference] to type [R] of [consumer].
     */
    private <R> void acceptAny(TriConsumer<R, T, Player> consumer, Object reference, Player context, Object payload) {
        consumer.accept((R) reference, (T) payload, context);
    }
}
