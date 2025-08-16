package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.NoxesiumApi
import com.noxcrew.noxesium.api.network.HandshakeState
import com.noxcrew.noxesium.api.network.NoxesiumPacket
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeAcknowledgePacket
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakeAcknowledgePacket
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakePacket
import com.noxcrew.noxesium.api.nms.network.HandshakePackets
import com.noxcrew.noxesium.api.nms.network.NoxesiumClientboundNetworking
import com.noxcrew.noxesium.paper.NoxesiumPaper
import com.noxcrew.noxesium.paper.nms
import net.minecraft.world.entity.player.Player
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent
import java.util.Base64
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Performs handshaking for Noxesium.
 */
public class NoxesiumServerHandshaker : Listener {
    private val handshakeState = mutableMapOf<UUID, HandshakeState>()
    private val pendingPackets = mutableMapOf<UUID, NoxesiumPacket>()

    /** Registers the handshaker. */
    public fun register() {
        // Listen to events for plugin channels being registered which indicates readiness
        Bukkit.getPluginManager().registerEvents(this, NoxesiumPaper.plugin)

        // TODO handle server rules and client settings in some other handler file!

        // Respond to incoming handshake packets
        HandshakePackets.SERVERBOUND_HANDSHAKE.addListener(this) { reference, packet, player ->
            reference.handleHandshake(player, packet)
        }
        HandshakePackets.SERVERBOUND_HANDSHAKE_ACKNOWLEDGE.addListener(this) { reference, packet, player ->
            reference.handleHandshakeAcknowledge(player, packet)
        }

        // Register the handshaking packets so clients know they can authenticate here!
        HandshakePackets.INSTANCE.register()
    }

    /** Handles a client initiating a handshake with the server. */
    private fun handleHandshake(player: Player, packet: ServerboundHandshakePacket) {
        println("Received $packet from ${player.gameProfile.name}")

        // Do not allow multiple handshakes!
        if (player.uuid in handshakeState) return

        // Accept all incoming entry points and respond to them
        val entrypoints = mutableMapOf<String, String>()

        // Prepare the encrypted ids of each entrypoint
        val encryptedIds =
            NoxesiumApi.getInstance().allEntrypoints.associateBy { entrypoint ->
                val id = entrypoint.id
                val encryptionKeyFile = entrypoint.encryptionKey
                if (encryptionKeyFile != null) {
                    try {
                        encryptionKeyFile.openStream().use { stream ->
                            val keyBytes = Base64.getDecoder().decode(stream.readAllBytes())
                            val keySpec = SecretKeySpec(keyBytes, "AES")
                            val iv = IvParameterSpec(byteArrayOf(-76, 14, 22, -123, 63, 60, -50, 23, -118, 10, 105, -127, 85, 41, -97, 37))
                            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
                            return@associateBy Base64.getEncoder().encodeToString(cipher.doFinal(id.toByteArray(Charsets.UTF_8)))
                        }
                    } catch (x: Exception) {
                        NoxesiumApi.getLogger().error("Failed to encrypt entrypoint id {}", id)
                    }
                }
                return@associateBy id
            }

        // Determine which entrypoints the server is aware of
        for ((id, challenge) in packet.entrypoints) {
            // Ignore any entrypoints we do not recognise!
            val entrypoint = encryptedIds[id] ?: continue

            // Decode the challenge if applicable
            val decodedChallenge =
                entrypoint.encryptionKey?.let { encryptionKeyFile ->
                    try {
                        encryptionKeyFile.openStream().use { stream ->
                            val keyBytes: ByteArray = Base64.getDecoder().decode(stream.readAllBytes())
                            val keySpec = SecretKeySpec(keyBytes, "AES")
                            val iv = IvParameterSpec(byteArrayOf(-76, 14, 22, -123, 63, 60, -50, 23, -118, 10, 105, -127, 85, 41, -97, 37))
                            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
                            return@let String(cipher.doFinal(Base64.getDecoder().decode(challenge)), Charsets.UTF_8)
                        }
                    } catch (x: Exception) {
                        NoxesiumApi.getLogger().error("Failed to decrypt challenge for entrypoint id {}", id)
                    }
                    null
                } ?: challenge

            // Add to the list of entrypoints
            entrypoints[entrypoint.id] = decodedChallenge
        }

        // Determine the response based on the current state
        if (entrypoints.isEmpty()) return
        val packet = ClientboundHandshakeAcknowledgePacket(entrypoints)
        if (NoxesiumClientboundNetworking.getInstance().canReceive(player, HandshakePackets.CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE)) {
            // The client has already indicated it can receive the acknowledgment packet,
            // send it immediately!
            NoxesiumClientboundNetworking.send(player, packet)
            handshakeState[player.uuid] = HandshakeState.AWAITING_RESPONSE
        } else {
            // The client hasn't sent that it can receive the acknowledgment packet yet, so
            // we put the packet in a pending list and wait for it.
            pendingPackets[player.uuid] = packet
            handshakeState[player.uuid] = HandshakeState.NONE
        }
    }

    /** Handle a successful handshake acknowledgement, register all packets, and call events. */
    private fun handleHandshakeAcknowledge(player: Player, packet: ServerboundHandshakeAcknowledgePacket) {
        println("Received $packet from ${player.gameProfile.name}")

        // Ignore unless we are awaiting this handshake
        if (handshakeState[player.uuid] != HandshakeState.AWAITING_RESPONSE) return
    }

    @EventHandler
    public fun onPlayerQuit(event: PlayerQuitEvent) {
        handshakeState -= event.player.uniqueId
        pendingPackets -= event.player.uniqueId
    }

    @EventHandler
    public fun onChannelRegistered(event: PlayerRegisterChannelEvent) {
        println("Registered ${event.channel}")

        // When specific packets are registered that we were waiting to send we send them!
        val player = event.player
        if (event.channel == HandshakePackets.CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE.id().toString()) {
            // Delay by a tick so the other handshake channels are also registered!
            Bukkit.getScheduler().scheduleSyncDelayedTask(
                NoxesiumPaper.plugin,
                {
                    if (!player.isConnected) return@scheduleSyncDelayedTask

                    // The client has requested the handshake, but it has not yet been
                    // sent the acknowledgement, send the pending packet now that we
                    // definitely can!
                    val packet = pendingPackets.remove(player.uniqueId) ?: return@scheduleSyncDelayedTask
                    NoxesiumClientboundNetworking.send(player.nms, packet)
                    handshakeState[player.uniqueId] = HandshakeState.AWAITING_RESPONSE
                },
            )
        }
    }
}
