package com.noxcrew.noxesium.api.network.payload;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.network.NoxesiumNetworking;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.PacketCollection;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a group of payload types which share the same versioned
 * channel ids.
 */
public class NoxesiumPayloadGroup {
    @NotNull
    private final List<NoxesiumPayloadType<?>> payloadTypes = new ArrayList<>();

    @NotNull
    private final PacketCollection packetCollection;

    @NotNull
    private final Key id;

    private final boolean clientToServer;
    private boolean lazy = false;

    /**
     * All listeners registered to this payload type.
     */
    private final Set<Triple<Class<?>, WeakReference<?>, TriConsumer<?, ?, UUID>>> listeners =
            ConcurrentHashMap.newKeySet();

    public NoxesiumPayloadGroup(@NotNull PacketCollection collection, @NotNull Key id, boolean clientToServer) {
        this.packetCollection = collection;
        this.id = id;
        this.clientToServer = clientToServer;
    }

    /**
     * Returns the packet collection this group belongs to.
     */
    @NotNull
    public PacketCollection getPacketCollection() {
        return packetCollection;
    }

    /**
     * Returns the id of this payload group.
     */
    public Key id() {
        return id;
    }

    /**
     * Returns whether this packet group is lazy.
     */
    public boolean isLazy() {
        return lazy;
    }

    /**
     * Marks this group as lazy, causing it to only be sent if listened to on the other side.
     */
    public NoxesiumPayloadGroup markLazy() {
        lazy = true;
        return this;
    }

    /**
     * Returns whether this group is sent from the client to the server, so serverbound.
     * If `false` this packet group is clientbound.
     */
    public boolean isClientToServer() {
        return clientToServer;
    }

    /**
     * Returns all payload types in this group.
     */
    @NotNull
    public List<NoxesiumPayloadType<?>> getPayloadTypes() {
        return payloadTypes;
    }

    /**
     * Adds a new packet to this group.
     *
     * @param clazz The class of type T.
     * @param <T>   The type of packet.
     */
    public <T extends NoxesiumPacket> NoxesiumPayloadGroup add(Class<T> clazz) {
        // Append p0, p1, etc. for the different protocol types.
        var newPayload = NoxesiumNetworking.getInstance()
                .createPayloadType(
                        this, Key.key(id.namespace(), id.value() + "-p" + payloadTypes.size()), clazz, clientToServer);
        payloadTypes.add(newPayload);
        packetCollection.addPluginChannelIdentifier(newPayload.id().toString());
        return this;
    }

    /**
     * Registers a new packet converter to this group which adds support
     * for converting between packet types to support multiple packet types
     * on the same packet listeners.
     *
     * @param previousClazz The class of type P.
     * @param newClass      The class of type T.
     * @param converter     A function that converts type P to T.
     * @param <T>           The type of the last packet.
     * @param <P>           The type of the new packet.
     */
    public <P extends NoxesiumPacket, T extends NoxesiumPacket> NoxesiumPayloadGroup converter(
            Class<P> previousClazz, Class<T> newClass, Function<P, T> converter) {
        // TODO Implement converters
        return this;
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

                // Ignore if the listener was garbage collected
                var obj = pair.getMiddle().get();
                if (obj == null) {
                    iterator.remove();
                    continue;
                }

                // Ignore handlers that want a different payload type
                var clazz = pair.getLeft();
                // TODO Support converters!
                if (!clazz.isInstance(payload)) continue;

                acceptAny(pair.getRight(), obj, context, payload);
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
    public <R, T extends NoxesiumPacket> void addListener(
            R reference, Class<T> clazz, @NotNull TriConsumer<R, T, UUID> listener) {
        var wasEmpty = !hasListeners();
        listeners.removeIf((it) -> it.getMiddle().get() == null);
        listeners.add(Triple.of(clazz, new WeakReference<>(reference), listener));

        // If this listener was previously not active, inform the other side!
        if (wasEmpty) {
            NoxesiumNetworking.getInstance().markLazyActive(this);
        }
    }

    /**
     * Casts [reference] to type [R] of [consumer].
     */
    private <R, T> void acceptAny(TriConsumer<R, T, UUID> consumer, Object reference, UUID context, Object payload) {
        consumer.accept((R) reference, (T) payload, context);
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
