package com.noxcrew.noxesium.api;

import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * An entrypoint for Noxesium which allows a mod or plugin to take control and register its features,
 * registry contents, packets, and more. Each set of features implemented against Noxesium's APIs
 * must be defined in an entrypoint.
 * <p>
 * All Noxesium entrypoints may provide both a private and public key. This system is used
 * by the common module to serve as an implementation example. Of course this means that the
 * common module's keys are all public in this repository which is intentional as it is only
 * meant to be an example. If you make your own private implementation you should of course
 * keep these files private and only ship the public server key with your client mod.
 * <p>
 * The system allows implementations to be shielded from unauthorized clients that do not have the
 * correct private key expected by the other side of the implementation. This prevents the client from
 * leaking any registries unless the server can correctly authenticate itself.
 * <p>
 * The handshake process for Noxesium runs for all entrypoints simultaneously, but includes
 * the following steps before it can be completed:
 * 1) The server-side implementation shares with the client that it can receive handshake packets.
 * 2) The client-side implementation sends a handshake initiation with a series of entrypoint ids
 * encrypted using a shared encryption key of each entrypoint when joining a server or entering the
 * configuration phase.
 * 3) The server-side implementation responds with an acknowledgement containing a list of entry
 * point ids it could decrypt in plain text.
 * 4) The client-side implementation responds with an acknowledgement containing a list of the
 * validated entrypoints and their protocol details. It registers all packet handlers for this entrypoint.
 * 5) The server-side implementation receives the answer, registers all plugin channels defined and
 * performs relevant initialization steps.
 * 6) The server-side sends all registry contents to the client. Receiving back an acknowledgement of
 * the registry entries which existed on the client and which ones did not.
 * 7) The server-side sends a handshake completion packet when it is fully done.
 */
public interface NoxesiumEntrypoint {
    /**
     * Returns the identifier of this entrypoint. This must be globally unique from
     * all other installed entrypoints.
     */
    String getId();

    /**
     * Returns instances for all features of this entrypoint. These can be re-created on
     * every initialization or re-used between re-initializations.
     */
    default Collection<NoxesiumFeature> getAllFeatures() {
        return List.of();
    }

    /**
     * Returns all registry collections included in this entrypoint.
     */
    default Collection<RegistryCollection<?>> getRegistryCollections() {
        return List.of();
    }

    /**
     * Returns an optional location of this entrypoint's base64 encoded AES-256 key resource to use for
     * securely authenticating that a valid client and server implementation of the same entrypoint are present.
     * <p>
     * An AES key is used as it is expected that anyone in possession of either sources is allowed to communicate
     * with the other side.
     */
    @Nullable
    default URL getEncryptionKey() {
        return null;
    }
}
