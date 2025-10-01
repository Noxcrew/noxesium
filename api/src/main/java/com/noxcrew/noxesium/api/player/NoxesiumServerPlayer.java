package com.noxcrew.noxesium.api.player;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.MutableNoxesiumComponentHolder;
import com.noxcrew.noxesium.api.component.SimpleMutableNoxesiumComponentHolder;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.network.ModInfo;
import com.noxcrew.noxesium.api.network.NoxesiumClientboundNetworking;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.NoxesiumRegistryDependentPacket;
import com.noxcrew.noxesium.api.network.handshake.HandshakeState;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadGroup;
import com.noxcrew.noxesium.api.player.sound.NoxesiumSound;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import com.noxcrew.noxesium.core.client.setting.ClientSettings;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundOpenLinkPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundUpdateGameComponentsPacket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Stores information on a player connected to a server running Noxesium's
 * server-side API.
 */
public class NoxesiumServerPlayer {
    @NotNull
    private final UUID uniqueId;

    @NotNull
    private final String username;

    @NotNull
    private final Component displayName;

    private final SimpleMutableNoxesiumComponentHolder components =
            new SimpleMutableNoxesiumComponentHolder(NoxesiumRegistries.GAME_COMPONENTS);
    private final List<Integer> pendingRegistrySyncs = new ArrayList<>();
    private final Map<NoxesiumRegistry<?>, Integer> registryHashes = new HashMap<>();
    private final Map<Integer, IdChangeSet> sentIndices = new HashMap<>();
    private final Map<NoxesiumRegistry<?>, Set<Integer>> knownIndices = new HashMap<>();
    private final Set<Key> capabilities = new HashSet<>();
    private final Set<Key> enabledLazyPackets = new HashSet<>();
    private final Set<String> clientRegisteredPluginChannels = new HashSet<>();
    private final Set<String> serverRegisteredPluginChannels = new HashSet<>();
    private final boolean isTransferred;

    @NotNull
    private HandshakeState handshakeState = HandshakeState.NONE;

    @NotNull
    private List<EntrypointProtocol> supportedEntrypoints = new ArrayList<>();

    @NotNull
    private List<String> supportedEntrypointIds = new ArrayList<>();

    @Nullable
    private ClientSettings settings;

    @NotNull
    private Set<NoxesiumPacket> pendingPackets = ConcurrentHashMap.newKeySet();

    @NotNull
    private Map<String, String> mods = new HashMap<>();

    private int lastSoundId = 0;
    private boolean dirty = false;
    private long lastPacket = System.currentTimeMillis();

    public NoxesiumServerPlayer(
            @NotNull final UUID uniqueId,
            @NotNull final String username,
            @NotNull final Component displayName,
            @Nullable final SerializedNoxesiumServerPlayer serializedPlayer) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.displayName = displayName;

        if (serializedPlayer != null) {
            // If serialized data is present, load it in!
            this.isTransferred = true;
            this.supportedEntrypoints = serializedPlayer.supportedEntrypoints();
            this.supportedEntrypointIds = this.supportedEntrypoints.stream()
                    .map(EntrypointProtocol::id)
                    .toList();
            this.capabilities.addAll(this.supportedEntrypoints.stream()
                    .flatMap((it) -> it.capabilities().stream())
                    .collect(Collectors.toSet()));
            this.mods = serializedPlayer.mods();
            this.settings = serializedPlayer.settings();
            for (var packet : serializedPlayer.enabledLazyPackets()) {
                this.enabledLazyPackets.add(Key.key(packet));
            }
            for (var entry : serializedPlayer.knownIds().entrySet()) {
                var registry = NoxesiumRegistries.REGISTRIES_BY_ID.get(Key.key(entry.getKey()));
                this.knownIndices.put(registry, entry.getValue());
            }
            for (var entry : serializedPlayer.knownHashes().entrySet()) {
                var registry = NoxesiumRegistries.REGISTRIES_BY_ID.get(Key.key(entry.getKey()));
                this.registryHashes.put(registry, entry.getValue());
            }
            this.clientRegisteredPluginChannels.addAll(serializedPlayer.clientRegisteredPluginChannels());
            this.serverRegisteredPluginChannels.addAll(serializedPlayer.serverRegisteredPluginChannels());
        } else {
            this.isTransferred = false;
        }
    }

    /**
     * Returns a serialized representation of this player's data.
     */
    @NotNull
    public SerializedNoxesiumServerPlayer serialize() {
        var copiedKnownIndices = new HashMap<String, Set<Integer>>();
        for (var entry : knownIndices.entrySet()) {
            copiedKnownIndices.put(entry.getKey().id().asString(), entry.getValue());
        }
        var copiedHashes = new HashMap<String, Integer>();
        for (var entry : registryHashes.entrySet()) {
            copiedHashes.put(entry.getKey().id().asString(), entry.getValue());
        }
        return new SerializedNoxesiumServerPlayer(
                supportedEntrypoints,
                settings,
                enabledLazyPackets.stream().map(Key::asString).collect(Collectors.toSet()),
                mods,
                copiedKnownIndices,
                copiedHashes,
                clientRegisteredPluginChannels,
                serverRegisteredPluginChannels);
    }

    /**
     * Returns the UUID of this player.
     */
    @NotNull
    public UUID getUniqueId() {
        return uniqueId;
    }

    /**
     * Returns the username of this player.
     */
    @NotNull
    public String getUsername() {
        return username;
    }

    /**
     * Returns the display name of this player.
     */
    @NotNull
    public Component getDisplayName() {
        return displayName;
    }

    /**
     * Returns the collection of all plugin channels the client has told the server about.
     * These are clientbound channels the server can send.
     */
    @NotNull
    public Collection<String> getClientRegisteredPluginChannels() {
        return clientRegisteredPluginChannels;
    }

    /**
     * Returns the collection of all plugin channels the server has told the client about.
     * These are serverbound channels the client can send.
     */
    @NotNull
    public Collection<String> getServerRegisteredPluginChannels() {
        return serverRegisteredPluginChannels;
    }

    /**
     * Returns whether the serializable data of this player has recently changed.
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Removes the dirty marking of this player.
     */
    public void unmarkDirty() {
        dirty = false;
    }

    /**
     * Returns whether this player object was transferred from another initial server.
     */
    public boolean isTransferred() {
        return isTransferred;
    }

    /**
     * Returns the current handshake state.
     */
    @NotNull
    public HandshakeState getHandshakeState() {
        return handshakeState;
    }

    /**
     * Sets the current handshake state.
     */
    public void setHandshakeState(@NotNull HandshakeState state) {
        this.handshakeState = state;
    }

    /**
     * Adds the given entrypoints to this player.
     */
    public void addEntrypoints(Collection<EntrypointProtocol> entrypoints) {
        for (var entrypoint : entrypoints) {
            supportedEntrypoints.add(entrypoint);
            supportedEntrypointIds.add(entrypoint.id());
            capabilities.addAll(entrypoint.capabilities());
        }
        this.dirty = true;
    }

    /**
     * Adds a list of channels to the client registered plugin channels.
     */
    public void addClientRegisteredPluginChannels(Collection<String> newChannels) {
        clientRegisteredPluginChannels.addAll(newChannels);
        dirty = true;
    }

    /**
     * Adds a list of channels to the server registered plugin channels.
     */
    public void addServerRegisteredPluginChannels(Collection<String> newChannels) {
        serverRegisteredPluginChannels.addAll(newChannels);
        dirty = true;
    }

    /**
     * Returns a mapping of all mods installed on this client
     * by mod id to their version.
     * These are as reported by the client and may be falsified.
     */
    public Map<String, String> getMods() {
        return mods;
    }

    /**
     * Sets the installed client mods.
     */
    public void setMods(Collection<ModInfo> mods) {
        var map = new HashMap<String, String>();
        for (var mod : mods) {
            map.put(mod.id(), mod.version());
        }
        this.mods = map;
        this.dirty = true;
    }

    /**
     * Returns whether this player wants to receive the given lazy packet type.
     */
    public boolean shouldSendLazyPacket(NoxesiumPayloadGroup group) {
        return !group.isLazy() || shouldSendLazyPacket(group.groupId());
    }

    /**
     * Returns whether this player wants to receive the given lazy packet type.
     */
    public boolean shouldSendLazyPacket(Key packet) {
        return enabledLazyPackets.contains(packet);
    }

    /**
     * Marks the given packets as having a client listener, meaning they
     * should be sent to this client.
     */
    public void addEnabledLazyPackets(Collection<Key> packets) {
        this.enabledLazyPackets.addAll(packets);
    }

    /**
     * Returns the base version of the mod.
     */
    public String getBaseVersion() {
        return supportedEntrypoints.stream()
                .filter(it -> it.id().equals(NoxesiumReferences.COMMON_ENTRYPOINT))
                .map(EntrypointProtocol::version)
                .findAny()
                .orElse("unknown");
    }

    /**
     * Returns a list of all entrypoints this client is using and can communicate
     * with the server through. This includes the specific protocol version being
     * used by the client.
     */
    @NotNull
    public List<EntrypointProtocol> getSupportedEntrypoints() {
        return supportedEntrypoints;
    }

    /**
     * Returns a list of the ids of all entrypoints this client can receive.
     */
    @NotNull
    public List<String> getSupportedEntrypointIds() {
        return supportedEntrypointIds;
    }

    /**
     * Returns whether this player supports the given capability.
     * These are client-reported keys that can be used to identify
     * certain features or systems the client supports.
     */
    public boolean hasCapability(Key capability) {
        return capabilities.contains(capability);
    }

    /**
     * Returns when a packet was last received from this player.
     */
    public long getLastPacketReceiveTime() {
        return lastPacket;
    }

    /**
     * Lists when the last packet was received.
     */
    public void markPacketReceived() {
        lastPacket = System.currentTimeMillis();
    }

    /**
     * Returns the last received settings defined by this client.
     */
    @Nullable
    public ClientSettings getClientSettings() {
        return settings;
    }

    /**
     * Updates the client settings of this client.
     */
    public void updateClientSettings(@NotNull ClientSettings settings) {
        this.settings = settings;
        this.dirty = true;
    }

    /**
     * Returns whether this player can receive the given key in the
     * given registry. This will only be `true` if the player has the
     * entrypoint required and was able to receive the data during
     * handshaking.
     */
    public <T> boolean isAwareOf(NoxesiumRegistry<T> registry, Key key) {
        var id = registry.getIdForKey(key);
        if (!knownIndices.containsKey(registry)) return false;
        return knownIndices.get(registry).contains(id);
    }

    /**
     * Returns if this client already knows the contents of the given registry
     * to equal the given hash.
     */
    public boolean isRegistrySynchronized(NoxesiumRegistry<?> registry, int hash) {
        var currentHash = registryHashes.get(registry);
        if (Objects.equals(currentHash, hash)) {
            return true;
        }
        registryHashes.put(registry, hash);
        dirty = true;
        return false;
    }

    /**
     * Marks the given registry as dynamic, that is its contents are changing
     * and need to be re-synced after a transfer.
     */
    public void markRegistryDynamic(NoxesiumRegistry<?> registry) {
        if (registryHashes.remove(registry) != null) {
            dirty = true;
        }
    }

    /**
     * Adds a new identifier to wait for with registry syncs.
     */
    public void awaitRegistrySync(int id, IdChangeSet ids) {
        pendingRegistrySyncs.add(id);
        sentIndices.put(id, ids);
    }

    /**
     * Returns whether one or more registries are currently being synchronized.
     */
    public boolean isAwaitingRegistries() {
        return !pendingRegistrySyncs.isEmpty();
    }

    /**
     * Handles acknowledgement of a registry synchronization.
     */
    public boolean acknowledgeRegistrySync(int id, Collection<Integer> missingIds) {
        if (pendingRegistrySyncs.contains(id)) {
            pendingRegistrySyncs.remove((Object) id);

            // Determine which indices were sent and update the local cache based on it
            var sent = sentIndices.remove(id);
            if (sent != null) {
                var registry = sent.registry();
                var known = knownIndices.getOrDefault(registry, new HashSet<>());
                if (sent.reset()) {
                    known.clear();
                }
                for (var added : sent.added()) {
                    if (missingIds.contains(added)) continue;
                    known.add(added);
                }
                known.removeAll(sent.removed());
                known.removeAll(missingIds);
                knownIndices.put(registry, known);
                dirty = true;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns if the handshake has been completed and informs
     * the client if it is.
     */
    public boolean isHandshakeCompleted() {
        // We can only complete the handshake if we are awaiting registries!
        if (handshakeState != HandshakeState.AWAITING_REGISTRIES) return false;

        // If we are waiting some registry sync to complete we cannot complete the handshake
        if (!pendingRegistrySyncs.isEmpty()) return false;

        // Check if every entrypoint has at least one channel registered
        for (var protocol : supportedEntrypoints) {
            var entrypoint = NoxesiumApi.getInstance().getEntrypoint(protocol.id());

            // This should never occur but just in case we just prevent the handshake from completing!
            if (entrypoint == null) return false;

            // Check for all channels in this entrypoint's collection if none of them are registered
            // we still need to wait! Check at least one serverbound and one clientbound.
            if (entrypoint instanceof NoxesiumEntrypoint nmsEntrypoint) {
                var serverboundChannels = nmsEntrypoint.getPacketCollections().stream()
                        .flatMap(it -> it.getPackets().stream())
                        .flatMap(it -> it.getPayloadTypes().stream())
                        .filter(it -> it.clientToServer)
                        .map(it -> it.id().toString())
                        .toList();
                var clientboundChannels = nmsEntrypoint.getPacketCollections().stream()
                        .flatMap(it -> it.getPackets().stream())
                        .flatMap(it -> it.getPayloadTypes().stream())
                        .filter(it -> !it.clientToServer)
                        .map(it -> it.id().toString())
                        .toList();

                if (!serverboundChannels.isEmpty()
                        && serverboundChannels.stream().noneMatch(serverRegisteredPluginChannels::contains))
                    return false;
                if (!clientboundChannels.isEmpty()
                        && clientboundChannels.stream().noneMatch(clientRegisteredPluginChannels::contains))
                    return false;
            }
        }
        return true;
    }

    /**
     * Sends the given packet, automatically detects the type of the packet based on the registered packets.
     */
    public boolean sendPacket(@NotNull NoxesiumPacket packet) {
        // Do not send packets if there is no handshake!
        if (getHandshakeState() == HandshakeState.NONE) return false;

        // Check if they can receive this packet
        var type = NoxesiumClientboundNetworking.getInstance().getPacketTypes().get(packet.getClass());
        if (type == null) return false;

        // Check if the client can receive this type before we queue the packet
        var instance = NoxesiumClientboundNetworking.getInstance();
        var transformedPacket =
                type.getGroup().convertIntoSupported(type, packet, (it) -> instance.canReceive(this, it));
        if (transformedPacket == null) return false;

        // If this packet updates some registry we halt it until we're done updating registries!
        if (isAwaitingRegistries() && packet instanceof NoxesiumRegistryDependentPacket) {
            pendingPackets.add(transformedPacket);
            return true;
        }
        type.sendClientboundAny(this, transformedPacket);
        return true;
    }

    /**
     * Ticks this player, sending out any pending update packets to the clients in
     * batches.
     */
    public void tick() {
        if (handshakeState != HandshakeState.COMPLETE) return;
        if (isAwaitingRegistries()) return;

        var pending = pendingPackets;
        pendingPackets = ConcurrentHashMap.newKeySet();
        pending.forEach(this::sendPacket);
        if (components.hasModified()) {
            sendPacket(new ClientboundUpdateGameComponentsPacket(false, components.collectModified(this)));
            components.clearModifications();
        }
    }

    /**
     * Returns the holder of game components for this player.
     */
    public MutableNoxesiumComponentHolder getGameComponents() {
        return components;
    }

    /**
     * Opens up a link dialog for this client pointing to the given URL.
     */
    public void openLink(String url) {
        openLink(url, null);
    }

    /**
     * Opens up a link dialog for this client pointing to the given URL while showing
     * the given text message.
     */
    public void openLink(String url, @Nullable Component text) {
        sendPacket(new ClientboundOpenLinkPacket(Optional.ofNullable(text), url));
    }

    /**
     * Plays a customisable sound effect to this player.
     *
     * @see playSound
     */
    public NoxesiumSound playSound(@NotNull Key sound, @NotNull Sound.Source source) {
        return playSound(sound, source, 1f, 1f, 0f, false, true);
    }

    /**
     * Plays a customisable sound effect to this player.
     *
     * @param sound       The id of the sound to play.
     * @param source      The sound source to play.
     * @param volume      The volume to play at.
     * @param pitch       The pitch to play at.
     * @param offset      An offset in seconds to offset the sound by.
     * @param looping     Whether the sound should continuously loop.
     * @param attenuation Whether the sound should attenuate. If `false` the sound is played at the same volume regardless of distance.
     */
    public NoxesiumSound playSound(
            @NotNull Key sound,
            @NotNull Sound.Source source,
            float volume,
            float pitch,
            float offset,
            boolean looping,
            boolean attenuation) {
        var noxesiumSound = new NoxesiumSound(
                this, lastSoundId++, sound, source, volume, pitch, offset, looping, attenuation, null, null);
        noxesiumSound.play(true);
        return noxesiumSound;
    }

    /**
     * Plays a customisable sound effect to this player at the given location.
     *
     * @see playSound
     */
    public NoxesiumSound playSound(@NotNull Vector3f position, @NotNull Key sound, @NotNull Sound.Source source) {
        return playSound(position, sound, source, 1f, 1f, 0f, false, true);
    }

    /**
     * Plays a customisable sound effect to this player at the given location.
     *
     * @see playSound
     */
    public NoxesiumSound playSound(
            @NotNull Vector3f position,
            @NotNull Key sound,
            @NotNull Sound.Source source,
            float volume,
            float pitch,
            float offset,
            boolean looping,
            boolean attenuation) {
        var noxesiumSound = new NoxesiumSound(
                this, lastSoundId++, sound, source, volume, pitch, offset, looping, attenuation, position, null);
        noxesiumSound.play(true);
        return noxesiumSound;
    }

    /**
     * Plays a customisable sound effect to this player bound to the given entity.
     *
     * @see playSound
     */
    public NoxesiumSound playSound(int entityId, @NotNull Key sound, @NotNull Sound.Source source) {
        return playSound(entityId, sound, source, 1f, 1f, 0f, false, true);
    }

    /**
     * Plays a customisable sound effect to this player bound to the given entity.
     *
     * @see playSound
     */
    public NoxesiumSound playSound(
            int entityId,
            @NotNull Key sound,
            @NotNull Sound.Source source,
            float volume,
            float pitch,
            float offset,
            boolean looping,
            boolean attenuation) {
        var noxesiumSound = new NoxesiumSound(
                this, lastSoundId++, sound, source, volume, pitch, offset, looping, attenuation, null, entityId);
        noxesiumSound.play(true);
        return noxesiumSound;
    }
}
