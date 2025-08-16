package com.noxcrew.noxesium.paper.api

import com.noxcrew.noxesium.api.NoxesiumReferences
import com.noxcrew.noxesium.core.client.setting.ClientSettings
import com.noxcrew.noxesium.paper.NoxesiumFeature
import com.noxcrew.noxesium.paper.api.event.NoxesiumPlayerRegisteredEvent
import com.noxcrew.noxesium.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundChangeServerRulesPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundServerInformationPacket
import com.noxcrew.noxesium.paper.api.rule.RemoteServerRule
import com.noxcrew.noxesium.paper.v0.NoxesiumListenerV0
import com.noxcrew.noxesium.paper.v1.NoxesiumListenerV1
import com.noxcrew.noxesium.paper.v2.NoxesiumListenerV2
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.slf4j.Logger
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * An instance of the Noxesium manager that handles management regardless of client protocol version.
 */
public open class NoxesiumManager(
    public val plugin: Plugin,
    public val logger: Logger,
) : Listener {
    private val players = ConcurrentHashMap<UUID, Int>()
    private val exact = ConcurrentHashMap<UUID, String>()
    private val settings = ConcurrentHashMap<UUID, ClientSettings>()
    private val profiles = ConcurrentHashMap<UUID, RuleHolder>()
    private val ready = ConcurrentHashMap.newKeySet<UUID>()
    private val pending = ConcurrentHashMap<UUID, Pair<Int, String>>()

    private lateinit var v0: BaseNoxesiumListener
    private lateinit var v1: BaseNoxesiumListener
    private lateinit var v2: BaseNoxesiumListener
    private var task: Int = -1

    /** Stores all registered server rules. */
    public val serverRules: RuleContainer = RuleContainer()

    /** Stores all registered entity rules. */
    public val entityRules: RuleContainer = RuleContainer()

    /** Returns that [playerId] is ready and has registered the plugin channels. */
    public fun isReady(playerId: UUID): Boolean = playerId in ready

    /**
     * Registers this manager and all listener variants.
     */
    public fun register() {
        v0 = NoxesiumListenerV0(plugin, logger, this).register()
        v1 = NoxesiumListenerV1(plugin, logger, this).register()
        v2 = NoxesiumListenerV2(plugin, logger, this).register()

        // Send server rule updates once a tick in a batch
        task =
            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
                for ((player, profile) in profiles) {
                    if (!profile.needsUpdate) continue
                    updateServerRules(Bukkit.getPlayer(player) ?: continue, profile)
                }
            }, 1, 1)

        // Register a player when we receive the client information packet
        NoxesiumPackets.SERVER_CLIENT_INFO.addListener(this) { packet, player ->
            // If the player hasn't told the server about their plugin channels yet we wait!
            if (player.uniqueId !in ready) {
                pending[player.uniqueId] = packet.protocolVersion to packet.versionString
                return@addListener
            }

            registerPlayer(player, packet.protocolVersion, packet.versionString)
        }
        NoxesiumPackets.SERVER_CLIENT_SETTINGS.addListener(this) { packet, player ->
            saveSettings(player, packet.settings)
        }

        // Register the event handler
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    /**
     * Unregisters this manager.
     */
    public fun unregister() {
        HandlerList.unregisterAll(this)

        Bukkit.getScheduler().cancelTask(task)

        v0.unregister()
        v1.unregister()
        v2.unregister()
    }

    /**
     * Marks that a player is ready to start receiving packets, that is they have confirmed with
     * the server the existence of at least one Noxesium channel.
     */
    public fun markReady(player: Player) {
        // Delay by a tick so the other channels can get registered. We assume all channels
        // are registered in one batch!
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
            if (!player.isConnected) return@scheduleSyncDelayedTask
            ready += player.uniqueId
            onReady(player)
            val (protocolVersion, version) = pending.remove(player.uniqueId) ?: return@scheduleSyncDelayedTask
            registerPlayer(player, protocolVersion, version)
        }, 1)
    }

    /** Run when [player] becomes ready. */
    protected open fun onReady(player: Player) {
    }

    /**
     * Sends this packet to the given [player].
     */
    public fun createPacket(player: Player, packet: NoxesiumPacket,): ClientboundCustomPayloadPacket? {
        val protocol = getProtocolVersion(player) ?: return null

        // Use the newest protocol this client supports!
        return if (protocol < NoxesiumFeature.API_V1.minProtocolVersion) {
            v0.createPacket(player, packet)
        } else if (protocol < NoxesiumFeature.API_V2.minProtocolVersion) {
            v1.createPacket(player, packet)
        } else {
            v2.createPacket(player, packet)
        }
    }

    /**
     * Sends this packet to the given [player].
     */
    public fun sendPacket(player: Player, packet: NoxesiumPacket,) {
        // Use the newest protocol this client supports!
        val nmsPacket = createPacket(player, packet) ?: return
        val craftPlayer = player as CraftPlayer
        craftPlayer.handle.connection.send(nmsPacket)
    }

    /**
     * Registers a new player [player] with the given [protocolVersion]. This sends
     * the player back various packets related to the server information and default
     * server rule values.
     */
    internal fun registerPlayer(player: Player, protocolVersion: Int, version: String,) {
        logger.info("${player.name} is running Noxesium $version ($protocolVersion)")
        saveProtocol(player, version, protocolVersion)

        // Inform the player about which version is being used
        sendPacket(player, ClientboundServerInformationPacket(NoxesiumReferences.VERSION))

        // Trigger a hook to use for implementations, this may be used
        // to set default values for the rules
        onPlayerRegistered(player)

        // Emit an event for hooking into
        Bukkit.getPluginManager().callEvent(NoxesiumPlayerRegisteredEvent(player, protocolVersion, version))

        // Send updated rules to this player
        updateServerRules(player)
    }

    /** Sends a packet to [player] to update any changed player rules immediately. */
    public fun updateServerRules(player: Player) {
        val profile = profiles[player.uniqueId] ?: return
        if (!profile.needsUpdate) return
        updateServerRules(player, profile)
    }

    /**
     * Sends updated server rules to [player] based on all rules in [profile]
     * that need to be updated.
     */
    private fun updateServerRules(player: Player, profile: RuleHolder,) {
        sendPacket(
            player,
            ClientboundChangeServerRulesPacket(
                profile.rules.filter { it.value.changePending }.mapValues { (_, rule) ->
                    { buffer ->
                        (rule as RemoteServerRule<Any>).write(rule.value, buffer)
                        rule.changePending = false
                    }
                },
            ),
        )
    }

    /** Stores the protocol version for [player] as [version] with [protocolVersion]. */
    internal fun saveProtocol(player: Player, version: String, protocolVersion: Int,) {
        players[player.uniqueId] = protocolVersion
        exact[player.uniqueId] = version
        onPlayerVersionReceived(player, version, protocolVersion)
    }

    /** Stores client settings for [player] as [clientSettings]. */
    internal fun saveSettings(player: Player, clientSettings: ClientSettings,) {
        settings[player.uniqueId] = clientSettings
        onPlayerSettingsReceived(player, clientSettings)
    }

    /** Returns the given [rule] for [player]. */
    public fun <T : Any> getServerRule(player: Player, rule: RuleFunction<T>,): RemoteServerRule<T>? = getServerRule(player, rule.index)

    public fun <T : Any> getServerRule(player: Player, index: Int,): RemoteServerRule<T>? =
        profiles.computeIfAbsent(player.uniqueId) { RuleHolder() }.let { holder ->
            serverRules.create(index, holder, getProtocolVersion(player) ?: -1)
        }

    public fun getClientSettings(player: Player): ClientSettings? = getClientSettings(player.uniqueId)

    public fun getClientSettings(playerId: UUID): ClientSettings? = settings[playerId]

    public fun getProtocolVersion(player: Player): Int? = getProtocolVersion(player.uniqueId)

    public fun getExactVersion(player: Player): String? = getExactVersion(player.uniqueId)

    public fun getProtocolVersion(playerId: UUID): Int? = players[playerId]

    public fun getExactVersion(playerId: UUID?): String? = exact[playerId]

    @EventHandler
    public fun onPlayerQuit(e: PlayerQuitEvent) {
        players -= e.player.uniqueId
        exact -= e.player.uniqueId
        settings -= e.player.uniqueId
        profiles -= e.player.uniqueId
        ready -= e.player.uniqueId
        pending -= e.player.uniqueId
    }

    /** Called when [player] is registered as a Noxesium user. */
    protected open fun onPlayerRegistered(player: Player) {
    }

    /** Called when [player] informs the server they are on [protocolVersion]. */
    protected open fun onPlayerVersionReceived(player: Player, version: String, protocolVersion: Int,) {
    }

    /** Called when [player] informs the server they are using [ClientSettings]. */
    protected open fun onPlayerSettingsReceived(player: Player, clientSettings: ClientSettings,) {
    }
}
