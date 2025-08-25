package com.noxcrew.noxesium.api.network.payload;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.NoxesiumClientboundNetworking;
import com.noxcrew.noxesium.api.network.NoxesiumNetworking;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A type of custom payload used by Noxesium for its packets.
 */
public class NoxesiumPayloadType<T extends NoxesiumPacket> {
    /**
     * The id of this payload type.
     */
    @NotNull
    private final Key id;

    /**
     * The class of the packet type.
     */
    @NotNull
    public final Class<T> clazz;

    /**
     * Whether this payload is sent from client to server.
     */
    public final boolean clientToServer;

    /**
     * All listeners registered to this payload type.
     */
    private final Set<Pair<WeakReference<?>, TriConsumer<?, T, UUID>>> listeners = ConcurrentHashMap.newKeySet();

    /**
     * Creates a new Noxesium payload type which can be listened to
     * by custom packet handlers.
     */
    public NoxesiumPayloadType(@NotNull Key id, @NotNull Class<T> clazz, boolean clientToServer) {
        this.id = id;
        this.clazz = clazz;
        this.clientToServer = clientToServer;
    }

    /**
     * Returns the id of this payload type.
     */
    public Key id() {
        return id;
    }

    /**
     * Returns the class of the packet payload type.
     */
    public Class<T> typeClass() {
        return clazz;
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
     * Sends the given [payload] as the type of this payload.
     */
    public boolean sendClientboundAny(NoxesiumServerPlayer player, Object payload) {
        return NoxesiumClientboundNetworking.getInstance().send(player, this, (T) payload);
    }

    /**
     * Sends the given [payload] as the type of this payload.
     */
    public boolean sendServerboundAny(Object payload) {
        return NoxesiumServerboundNetworking.getInstance().send(this, (T) payload);
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
    public void handle(@NotNull UUID context, @NotNull Object payload) {
        try {
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
        } catch (Throwable x) {
            NoxesiumApi.getLogger().error("Caught exception while handling packet", x);
        }
    }

    /**
     * Registers a new listener to a packet payload of this type. Garbage collection
     * of this listener is tied to the lifetime of the reference. The reference object should
     * only ever be referenced from within the listener using the passed instance. This prevents
     * the listener from holding its own reference captive. If you do this the listener
     * will never be properly garbage collected.
     * <p>
     * The listener receives the reference instance, the payload, and the UUID of the relevant
     * player. On the client this will always be the local client's uuid.
     */
    public <R> void addListener(R reference, @NotNull TriConsumer<R, T, UUID> listener) {
        listeners.removeIf((it) -> it.getKey().get() == null);
        listeners.add(Pair.of(new WeakReference<>(reference), listener));
    }

    /**
     * Casts [reference] to type [R] of [consumer].
     */
    private <R> void acceptAny(TriConsumer<R, T, UUID> consumer, Object reference, UUID context, Object payload) {
        consumer.accept((R) reference, (T) payload, context);
    }
}
