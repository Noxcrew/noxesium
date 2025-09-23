package com.noxcrew.noxesium.api.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

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
        return type.sendServerboundAny(packet);
    }

    @NotNull
    private ConnectionProtocolType configuredProtocol = ConnectionProtocolType.NONE;

    @NotNull
    private ConnectionProtocolType boundProtocol = ConnectionProtocolType.NONE;

    private final Set<Key> enabledLazyPackets = new HashSet<>();

    /**
     * Returns whether the given lazy packet type should be sent.
     */
    public boolean shouldSendLazy(NoxesiumPayloadType<?> type) {
        return (!type.lazy || shouldSendLazy(type.id())) && canSend(type);
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
            getPacketTypes().values().forEach(it -> it.unbind(boundProtocol));
        } else {
            // Only re-bind if the protocol isn't already bound to!
            // Otherwise we have already bound and we can ignore it.
            if (boundProtocol == ConnectionProtocolType.NONE) {
                getPacketTypes().values().forEach(it -> it.bind(activeProtocol));
            }
        }

        // Store what protocol we bound against
        boundProtocol = activeProtocol;
    }

    /**
     * Checks if the connected server should be receiving packets of the given type.
     *
     * @param packet The packet class
     * @return Whether the connected server should be receiving the packet
     */
    public boolean canSend(Class<?> packet) {
        var type = packetTypes.get(packet);
        if (type == null) return false;
        return canSend(type);
    }

    /**
     * Checks if the connected server should be receiving packets of the given type.
     *
     * @param type The packet type
     * @return Whether the connected server should be receiving the packet
     */
    public abstract boolean canSend(NoxesiumPayloadType<?> type);

    /**
     * Sends this packet to the currently connected server, if possible. Returns whether the packet
     * was successfully sent or not.
     */
    public abstract <T extends NoxesiumPacket> boolean send(NoxesiumPayloadType<T> type, T payload);
}
