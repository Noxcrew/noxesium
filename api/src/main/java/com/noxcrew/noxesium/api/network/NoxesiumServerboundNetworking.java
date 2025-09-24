package com.noxcrew.noxesium.api.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadGroup;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Sets up the Noxesium networking system in the serverbound direction.
 */
public abstract class NoxesiumServerboundNetworking extends NoxesiumNetworking {
    /**
     * Returns the singleton instance of this class.
     */
    public static NoxesiumServerboundNetworking getInstance() {
        Preconditions.checkNotNull(instance, "Cannot get networking instance before it is defined");
        return (NoxesiumServerboundNetworking) instance;
    }

    /**
     * Sends the given packet, automatically detects the type of the packet based on the registered packets.
     */
    public static boolean send(NoxesiumPacket packet) {
        var type = getInstance().getPacketTypes().get(packet.getClass());
        if (type == null) return false;
        var instance = NoxesiumServerboundNetworking.getInstance();
        if (!instance.canSend(type)) return false;
        type.sendServerboundAny(packet);
        return true;
    }

    @NotNull
    private ConnectionProtocolType configuredProtocol = ConnectionProtocolType.NONE;

    @NotNull
    private ConnectionProtocolType boundProtocol = ConnectionProtocolType.NONE;

    private final Set<Key> enabledLazyPackets = new HashSet<>();

    /**
     * Returns whether the given lazy packet type should be sent.
     */
    public boolean shouldSendLazy(NoxesiumPayloadGroup group) {
        return !group.isLazy() || shouldSendLazy(group.id());
    }

    /**
     * Returns whether the given lazy packet type should be sent.
     */
    public boolean shouldSendLazy(Key type) {
        return enabledLazyPackets.contains(type);
    }

    /**
     * Marks the given packets as having a server listener, meaning they
     * should be sent to the current server.
     */
    public void addEnabledLazyPackets(Collection<Key> packets) {
        this.enabledLazyPackets.addAll(packets);
    }

    /**
     * Resets the list of enabled lazy packets.
     */
    public void resetEnablesLazyPackets() {
        this.enabledLazyPackets.clear();
    }

    /**
     * Returns the protocol that has been configured.
     */
    @NotNull
    public ConnectionProtocolType getConfiguredProtocol() {
        return configuredProtocol;
    }

    /**
     * Returns the protocol Minecraft has initialized.
     */
    @NotNull
    public abstract ConnectionProtocolType getMinecraftProtocol();

    /**
     * Handles the current protocol changing to the given protocol type.
     */
    public void setConfiguredProtocol(ConnectionProtocolType protocolType) {
        // Ignore if unchanged
        if (configuredProtocol == protocolType) return;
        configuredProtocol = protocolType;

        // We can only bind if the active protocol is a match as it requires accessing the connection
        // of the current protocol type
        var activeProtocol = getMinecraftProtocol();

        // If the active protocol is different from what we last bound against the receivers
        // have been automatically destroyed as the addon has been destroyed.
        if (activeProtocol != boundProtocol) {
            boundProtocol = ConnectionProtocolType.NONE;
        }

        if (protocolType == ConnectionProtocolType.NONE) {
            // Only unbind if it's currently bound to PLAY!
            if (boundProtocol == ConnectionProtocolType.PLAY) {
                getPacketTypes().values().forEach(it -> it.unbind(boundProtocol));
            }
        } else {
            // Only re-bind if the protocol isn't already bound to!
            // Otherwise, we have already bound and we can ignore it.

            // Also, only bind to the play protocol!
            if (boundProtocol == ConnectionProtocolType.NONE && activeProtocol == ConnectionProtocolType.PLAY) {
                getPacketTypes().values().forEach(it -> it.bind(boundProtocol));

                // Store what protocol we bound against
                boundProtocol = activeProtocol;
            }
        }
    }

    @Override
    public void register(NoxesiumPayloadGroup payloadGroup, @Nullable NoxesiumEntrypoint entrypoint) {
        super.register(payloadGroup, entrypoint);
        if (boundProtocol == ConnectionProtocolType.PLAY) {
            for (var payloadType : payloadGroup.getPayloadTypes()) {
                payloadType.bind(boundProtocol);
            }
        }
    }

    @Override
    public void unregister(NoxesiumPayloadGroup payloadGroup) {
        super.unregister(payloadGroup);
        if (boundProtocol == ConnectionProtocolType.PLAY) {
            for (var payloadType : payloadGroup.getPayloadTypes()) {
                payloadType.unbind(boundProtocol);
            }
        }
    }

    /**
     * Checks if the connected server can receive packets of the given type.
     *
     * @param type The packet type
     * @return Whether this client can send the given packet
     */
    public <T extends NoxesiumPacket> boolean canSend(@NotNull NoxesiumPayloadType<T> type) {
        return type.getGroup().getPacketCollection().isRegistered();
    }

    /**
     * Sends this packet to the currently connected server, if possible.
     */
    public abstract <T extends NoxesiumPacket> void send(@NotNull NoxesiumPayloadType<T> type, T payload);
}
