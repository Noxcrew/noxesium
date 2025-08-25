package com.noxcrew.noxesium.api.player;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.MutableNoxesiumComponentHolder;
import com.noxcrew.noxesium.api.component.SimpleMutableNoxesiumComponentHolder;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.network.NoxesiumClientboundNetworking;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.handshake.HandshakeState;
import com.noxcrew.noxesium.api.player.sound.NoxesiumSound;
import com.noxcrew.noxesium.core.client.setting.ClientSettings;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundOpenLinkPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundUpdateGameComponentsPacket;
import com.noxcrew.noxesium.core.network.sync.clientbound.ClientboundRequestSyncEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    @NotNull
    private HandshakeState handshakeState = HandshakeState.NONE;

    @NotNull
    private final List<EntrypointProtocol> supportedEntrypoints = new ArrayList<>();

    @NotNull
    private final List<String> supportedEntrypointIds = new ArrayList<>();

    @NotNull
    private final List<Integer> pendingRegistrySyncs = new ArrayList<>();

    @Nullable
    private ClientSettings settings;

    @NotNull
    private SimpleMutableNoxesiumComponentHolder components = new SimpleMutableNoxesiumComponentHolder();

    private int lastSoundId;

    public NoxesiumServerPlayer(
            @NotNull final UUID uniqueId, @NotNull final String username, @NotNull final Component displayName) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.displayName = displayName;
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
     * Returns the current handshake state.
     */
    @NotNull
    public HandshakeState getHandshakeState() {
        return handshakeState;
    }

    /**
     * Sets the current handshake state.
     */
    public void setHandshakeState(HandshakeState state) {
        this.handshakeState = state;
    }

    /**
     * Adds the given entrypoints to this player.
     */
    public void addEntrypoints(Collection<EntrypointProtocol> entrypoints) {
        for (var entrypoint : entrypoints) {
            supportedEntrypoints.add(entrypoint);
            supportedEntrypointIds.add(entrypoint.id());
        }
    }

    /**
     * Returns the base version of the mod.
     */
    public String getBaseVersion() {
        return supportedEntrypoints.stream()
                .filter(it -> it.id().equals(NoxesiumReferences.COMMON_ENTRYPOINT))
                .map(EntrypointProtocol::rawVersion)
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
    }

    /**
     * Adds a new identifier to wait for with registry syncs.
     */
    public void awaitRegistrySync(int id) {
        pendingRegistrySyncs.add(id);
    }

    /**
     * Handles acknowledgement of a registry synchronization.
     */
    public boolean acknowledgeRegistrySync(int id) {
        if (pendingRegistrySyncs.contains(id)) {
            pendingRegistrySyncs.remove((Object) id);
            return true;
        }
        return false;
    }

    /**
     * Returns if the handshake has been completed and informs
     * the client if it is.
     */
    public boolean isHandshakeCompleted() {
        // If we are waiting some registry sync to complete we cannot complete the handshake
        if (!pendingRegistrySyncs.isEmpty()) return false;

        // Check if every entrypoint has at least one channel registered
        var registeredChannels = NoxesiumClientboundNetworking.getInstance().getRegisteredChannels(this);
        for (var protocol : supportedEntrypoints) {
            var entrypoint = NoxesiumApi.getInstance().getEntrypoint(protocol.id());

            // This should never occur but just in case we just prevent the handshake from completing!
            if (entrypoint == null) return false;

            // Check for all channels in this entrypoint's collection if none of them are registered
            // we still need to wait!
            if (entrypoint instanceof NoxesiumEntrypoint nmsEntrypoint) {
                var channels = nmsEntrypoint.getPacketCollections().stream()
                        .flatMap(it -> it.getPackets().stream())
                        .map(it -> it.id().toString());
                if (channels.noneMatch(registeredChannels::contains)) return false;
            }
        }
        return true;
    }

    /**
     * Sends the given packet, automatically detects the type of the packet based on the registered packets.
     */
    public boolean sendPacket(@NotNull NoxesiumPacket packet) {
        return NoxesiumClientboundNetworking.send(this, packet);
    }

    /**
     * Ticks this player, sending out any pending update packets to the clients in
     * batches.
     */
    public void tick() {
        if (components.hasModified()) {
            sendPacket(new ClientboundUpdateGameComponentsPacket(false, components.collectModified()));
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

    /**
     * Starts the folder syncing protocol with this player over the given
     * folder id. This will cause the client to receive a pop-up asking
     * them to confirm this sync and to pick a target directory.
     */
    public void startFolderSync(String folderId) {
        sendPacket(new ClientboundRequestSyncEvent(folderId));
    }
}
