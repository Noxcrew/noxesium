package com.noxcrew.noxesium.api.network.payload;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.network.NoxesiumNetworking;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.api.util.Pair;
import com.noxcrew.noxesium.api.util.TriConsumer;
import com.noxcrew.noxesium.api.util.Triple;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines a group of payload types which share the same versioned
 * channel ids.
 */
public class NoxesiumPayloadGroup {
    /**
     * Stores data for an ongoing chain of convertable packets.
     *
     * @param group    The group this chain belongs to.
     * @param oldClazz The last packet's class.
     * @param oldType  The last packet's type.
     * @param <P>      The type of the last packet.
     */
    public record NoxesiumPayloadGroupChain<P extends NoxesiumPacket>(
            NoxesiumPayloadGroup group, Class<P> oldClazz, NoxesiumPayloadType<P> oldType) {
        /**
         * Adds a new packet to this chain.
         *
         * @param clazz    The class of type T.
         * @param newToOld A function that converts type T to P.
         * @param oldToNew A function that converts type P to T.
         * @param <T>      The type of packet.
         */
        public <T extends NoxesiumPacket> NoxesiumPayloadGroupChain<T> add(
                Class<T> clazz, Function<T, P> newToOld, Function<P, T> oldToNew) {
            var newType = group.addType(clazz);
            group.addConverter(oldClazz, oldType, clazz, newType, newToOld, oldToNew);
            return new NoxesiumPayloadGroupChain<>(group, clazz, newType);
        }
    }

    @NotNull
    private final List<NoxesiumPayloadType<?>> payloadTypes = new ArrayList<>();

    @NotNull
    private final PacketCollection packetCollection;

    @NotNull
    private final Key id;

    private final Map<Class<? extends NoxesiumPacket>, Pair<NoxesiumPayloadType<?>, Function<?, ?>>> newToOld =
            new HashMap<>();
    private final Map<Class<? extends NoxesiumPacket>, Pair<NoxesiumPayloadType<?>, Function<?, ?>>> oldToNew =
            new HashMap<>();
    private final List<String> channelIds = new ArrayList<>();
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
    public Key groupId() {
        return id;
    }

    /**
     * Returns whether this packet group is lazy.
     */
    public boolean isLazy() {
        return lazy;
    }

    /**
     * Returns the raw strings of all ids in this group.
     */
    public List<String> getChannelIds() {
        return channelIds;
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
     * Converts the given packet into the first version that the given condition supports.
     *
     * @param type      The type of the packet.
     * @param packet    The actual packet instance.
     * @param condition A condition that checks if a type is valid.
     * @return The converted packet of a valid type.
     */
    @Nullable
    public NoxesiumPacket convertIntoSupported(
            NoxesiumPayloadType<?> type, NoxesiumPacket packet, Function<NoxesiumPayloadType<?>, Boolean> condition) {
        // Send packets across as the oldest version the client supports! Don't make packets more modern on the
        // server-side,
        // client handlers can transform packets to newer/older types.
        return searchForVersion(type, packet, condition, newToOld);
    }

    /**
     * Checks if this type or any it can convert into pass the given predicate.
     *
     * @param type      The type to check.
     * @param condition A condition that checks for the result.
     * @return The result from any condition.
     */
    public boolean checkAnySupports(NoxesiumPayloadType<?> type, Function<NoxesiumPayloadType<?>, Boolean> condition) {
        // First we figure out a valid list of converters that gets us to a valid packet!
        var currentType = type;
        while (!condition.apply(currentType)) {
            var pair = newToOld.get(currentType.clazz);
            if (pair == null) return false;
            currentType = pair.first();
        }
        return true;
    }

    /**
     * Applies a converter to two unknown packets.
     */
    private <T, P> NoxesiumPacket applyConverter(Function<T, P> converter, Object packet) {
        return (NoxesiumPacket) converter.apply((T) packet);
    }

    /**
     * Adds a new packet to this group.
     *
     * @param clazz The class of type T.
     * @param <T>   The type of packet.
     */
    public <T extends NoxesiumPacket> NoxesiumPayloadGroup add(Class<T> clazz) {
        addType(clazz);
        return this;
    }

    /**
     * Adds a new packet that is the start of a chain of backwards compatible
     * packets.
     *
     * @param clazz The class of type T.
     * @param <T>   The type of packet.
     */
    public <T extends NoxesiumPacket> NoxesiumPayloadGroupChain<T> chain(Class<T> clazz) {
        var type = addType(clazz);
        return new NoxesiumPayloadGroupChain<>(this, clazz, type);
    }

    /**
     * Registers a new payload type.
     *
     * @param clazz The class of type T.
     * @param <T>   The type of packet.
     * @return The newly created payload type.
     */
    private <T extends NoxesiumPacket> NoxesiumPayloadType<T> addType(Class<T> clazz) {
        // Append p0, p1, etc. for the different protocol types.
        var newPayload = NoxesiumNetworking.getInstance()
                .createPayloadType(
                        this,
                        Key.key(id.namespace(), id.value() + "-p" + (payloadTypes.size() + 1)),
                        clazz,
                        clientToServer);
        payloadTypes.add(newPayload);
        channelIds.add(newPayload.id().asString());
        if (clientToServer) {
            packetCollection.addPluginChannelIdentifier(newPayload.id().toString());
        }
        return newPayload;
    }

    /**
     * Registers a new packet converter to this group which adds support
     * for converting between packet types to support multiple packet types
     * on the same packet listeners. This requires a converter to create an
     * older packet type, we always send the newest packet type available but
     * downgrade to an older type if required by an older client/server.
     */
    private <P extends NoxesiumPacket, T extends NoxesiumPacket> void addConverter(
            Class<P> oldClass,
            NoxesiumPayloadType<P> oldType,
            Class<T> newClass,
            NoxesiumPayloadType<T> newType,
            Function<T, P> newToOld,
            Function<P, T> oldToNew) {
        this.newToOld.put(newClass, new Pair<>(newType, newToOld));
        this.oldToNew.put(oldClass, new Pair<>(oldType, oldToNew));
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
            // Start by determining the type of the payload
            var type = NoxesiumNetworking.getInstance().getPacketTypes().get(payload.getClass());
            if (type == null) return;

            // Cache converted types if multiple listeners need them
            var types = new HashMap<Class<?>, Optional<Object>>();
            types.put(payload.getClass(), Optional.of(payload));

            var iterator = listeners.iterator();
            while (iterator.hasNext()) {
                var pair = iterator.next();

                // Ignore if the listener was garbage collected
                var obj = pair.second().get();
                if (obj == null) {
                    iterator.remove();
                    continue;
                }

                // Try to cast the payload into the type this handler is attempting to receive
                var clazz = pair.first();
                var relevantPayload = types.computeIfAbsent(clazz, (target) -> {
                    // Try to modernize this packet to any newer types we know about that we might be listening for!
                    var newest = searchForVersion(type, payload, (it) -> it.clazz.equals(target), oldToNew);
                    if (newest != null) return Optional.of(newest);

                    // Failing that, try to make it older to see if we are still listening to outdated packets?
                    var oldest = searchForVersion(type, payload, (it) -> it.clazz.equals(target), newToOld);
                    if (oldest != null) return Optional.of(oldest);

                    return Optional.empty();
                });
                if (relevantPayload.isEmpty()) continue;
                acceptAny(pair.third(), obj, context, relevantPayload.get());
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
        listeners.removeIf((it) -> it.second().get() == null);
        listeners.add(new Triple<>(clazz, new WeakReference<>(reference), listener));

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

    /**
     * Converts the given packet into its last form in the given map.
     */
    @Nullable
    private NoxesiumPacket searchForVersion(
            NoxesiumPayloadType<?> type,
            Object packet,
            Function<NoxesiumPayloadType<?>, Boolean> condition,
            Map<Class<? extends NoxesiumPacket>, Pair<NoxesiumPayloadType<?>, Function<?, ?>>> map) {
        var selected = new HashSet<Function<?, ?>>();

        // First we figure out a valid list of converters that gets us to a valid packet!
        var currentType = type;
        while (!condition.apply(currentType)) {
            var pair = map.get(currentType.clazz);
            if (pair == null) {
                // If this payload type is not valid, and we cannot convert it into anything, give up!
                return null;
            }

            // Perform this conversion later and update the current type
            selected.add(pair.second());
            currentType = pair.first();
        }

        // Then we apply the found converters
        var currentPacket = packet;
        for (var converter : selected) {
            currentPacket = applyConverter(converter, currentPacket);
        }
        return (NoxesiumPacket) currentPacket;
    }
}
