package com.noxcrew.noxesium.paper.api

import com.noxcrew.noxesium.api.protocol.ClientSettings
import com.noxcrew.noxesium.api.protocol.NoxesiumFeature
import com.noxcrew.noxesium.api.protocol.NoxesiumServerManager
import com.noxcrew.noxesium.api.protocol.ProtocolVersion
import com.noxcrew.noxesium.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundChangeServerRulesPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundServerInformationPacket
import com.noxcrew.noxesium.paper.api.rule.RemoteServerRule
import com.noxcrew.noxesium.paper.api.rule.RuleFunction
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
) : NoxesiumServerManager<Player>, Listener {

    /** Stores information sent to a client. */
    public data class NoxesiumProfile(
        /** All rules sent to this client. */
        public val rules: MutableMap<Int, RemoteServerRule<*>> = ConcurrentHashMap(),
    ) {

        /** Whether this profile has pending updates. */
        public val needsUpdate: Boolean
            get() = rules.values.any { it.changePending }
    }

    private val players = ConcurrentHashMap<UUID, Int>()
    private val settings = ConcurrentHashMap<UUID, ClientSettings>()
    private val profiles = ConcurrentHashMap<UUID, NoxesiumProfile>()
    private val rules = ConcurrentHashMap<Int, RuleFunction<*>>()
    private val minimumProtocols = ConcurrentHashMap<Int, Int>()
    private val ready = ConcurrentHashMap.newKeySet<UUID>()
    private val pending = ConcurrentHashMap<UUID, Pair<Int, String>>()

    private lateinit var v0: BaseNoxesiumListener
    private lateinit var v1: BaseNoxesiumListener
    private lateinit var v2: BaseNoxesiumListener

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

        v0.unregister()
        v1.unregister()
        v2.unregister()
    }

    /**
     * Marks that a player is ready to start receiving packets, that is they have confirmed with
     * the server the existence of at least one Noxesium channel.
     */
    public fun markReady(player: Player) {
        ready += player.uniqueId
        onReady(player)
        val (protocolVersion, version) = pending.remove(player.uniqueId) ?: return
        registerPlayer(player, protocolVersion, version)
    }

    /** Run when [player] becomes ready. */
    protected open fun onReady(player: Player) {
    }

    /**
     * Sends this packet to the given [player].
     */
    public fun createPacket(player: Player, packet: NoxesiumPacket): ClientboundCustomPayloadPacket? {
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
    public fun sendPacket(player: Player, packet: NoxesiumPacket) {
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
    internal fun registerPlayer(player: Player, protocolVersion: Int, version: String) {
        logger.info("${player.name} is running Noxesium $version ($protocolVersion)")
        saveProtocol(player, version, protocolVersion)

        // Inform the player about which version is being used
        sendPacket(player, ClientboundServerInformationPacket(ProtocolVersion.VERSION))

        // Create the initial server rule objects for this player
        val profile = profiles.computeIfAbsent(player.uniqueId) { NoxesiumProfile() }
        for ((index, rule) in rules) {
            profile.rules.computeIfAbsent(index) {
                rule.function(player, index)
            }
        }

        // Trigger a hook to use for implementations, this may be used
        // to set default values for the rules
        onPlayerRegistered(player)

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
    private fun updateServerRules(player: Player, profile: NoxesiumProfile) {
        sendPacket(
            player,
            ClientboundChangeServerRulesPacket(
                profile.rules.filter { it.value.changePending }.mapValues { (_, rule) ->
                    { buffer ->
                        (rule as RemoteServerRule<Any>).write(rule.value, buffer)
                        rule.changePending = false
                    }
                }
            )
        )
    }

    /** Registers a new server rule with the given [index] and [ruleSupplier]. */
    public fun registerServerRule(index: Int, minimumProtocol: Int, ruleSupplier: RuleFunction<*>) {
        require(!rules.containsKey(index)) { "Can't double register index $index" }
        rules[index] = ruleSupplier
        minimumProtocols[index] = minimumProtocol
    }

    /** Stores the protocol version for [player] as [version] with [protocolVersion]. */
    internal fun saveProtocol(player: Player, version: String, protocolVersion: Int) {
        players[player.uniqueId] = protocolVersion
        onPlayerVersionReceived(player, version, protocolVersion)
    }

    /** Stores client settings for [player] as [clientSettings]. */
    internal fun saveSettings(player: Player, clientSettings: ClientSettings) {
        settings[player.uniqueId] = clientSettings
        onPlayerSettingsReceived(player, clientSettings)
    }

    /** Returns the given [rule] for [player]. */
    public fun <T : Any> getServerRule(player: Player, rule: RuleFunction<T>): RemoteServerRule<T>? =
        getServerRule(player, rule.index)

    override fun <T : Any> getServerRule(player: Player, index: Int): RemoteServerRule<T>? =
        profiles.computeIfAbsent(player.uniqueId) { NoxesiumProfile() }.let { profile ->
            // Ensure that this player has the required protocol version, otherwise return `null`.
            val version = getProtocolVersion(player) ?: -1
            if (version < (minimumProtocols[index] ?: throw IllegalArgumentException("Cannot find rule with index $index"))) return@let null
            return@let profile.rules
                .computeIfAbsent(index) {
                    val function = rules[index] ?: throw IllegalArgumentException("Cannot find rule with index $index")
                    function.function(player, index)
                } as RemoteServerRule<T>?
        }

    override fun getClientSettings(player: Player): ClientSettings? =
        getClientSettings(player.uniqueId)

    override fun getClientSettings(playerId: UUID): ClientSettings? =
        settings[playerId]

    override fun getProtocolVersion(player: Player): Int? =
        getProtocolVersion(player.uniqueId)

    override fun getProtocolVersion(playerId: UUID): Int? =
        players[playerId]

    @EventHandler
    public fun onPlayerQuit(e: PlayerQuitEvent) {
        players -= e.player.uniqueId
        settings -= e.player.uniqueId
        profiles -= e.player.uniqueId
        ready - e.player.uniqueId
        pending -= e.player.uniqueId
    }

    /** Called when [player] is registered as a Noxesium user. */
    protected open fun onPlayerRegistered(player: Player) {
    }

    /** Called when [player] informs the server they are on [protocolVersion]. */
    protected open fun onPlayerVersionReceived(player: Player, version: String, protocolVersion: Int) {
    }

    /** Called when [player] informs the server they are using [ClientSettings]. */
    protected open fun onPlayerSettingsReceived(player: Player, clientSettings: ClientSettings) {
    }
}
