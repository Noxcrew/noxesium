package com.noxcrew.noxesium.paper.api

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundSetExtraEntityDataPacket
import com.noxcrew.noxesium.paper.api.rule.RemoteServerRule
import io.papermc.paper.event.player.PlayerTrackEntityEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import java.util.WeakHashMap

/**
 * Provides a simple implementation for changing entity rules on entities.
 *
 * This is not used internally, so you should use it as a reference implementation
 * to tweak yourself. Feel free to merge any improvements you make upstream.
 *
 * For reference, MCC uses a custom implementation of this baked into our own
 * fake entity system where we send all entities entirely through packets and manage
 * them custom, hence we do not need a system such as this.
 */
public class EntityRuleManager(private val manager: NoxesiumManager) : Listener {

    private val entities = WeakHashMap<Entity, RuleHolder>()
    private var task: Int = -1

    /**
     * Registers this manager.
     */
    public fun register() {
        Bukkit.getPluginManager().registerEvents(this, manager.plugin)

        // Send rule updates once a tick in a batch
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(manager.plugin, {
            for ((entity, holder) in entities) {
                if (!holder.needsUpdate) continue
                
                // Send the packet to all players that can see it
                for (player in Bukkit.getOnlinePlayers()) {
                    if (!player.canSee(entity)) continue
                    manager.sendPacket(player,
                        ClientboundSetExtraEntityDataPacket(
                            entity.entityId,
                            holder.rules
                                // Only include rules that need to be updated!
                                .filter { it.value.changePending }
                                // Only include rules that are available to this player!
                                .filter { manager.entityRules.isAvailable(it.key, manager.getProtocolVersion(player) ?: -1) }
                                .ifEmpty { null }
                                ?.mapValues { (_, rule) ->
                                    { buffer -> (rule as RemoteServerRule<Any>).write(rule.value, buffer) }
                                } ?: continue
                        )
                    )
                } 
                
                // Mark all rules as updated
                holder.markAllUpdated()
            }
        }, 1, 1)
    }

    /**
     * Unregisters this manager.
     */
    public fun unregister() {
        HandlerList.unregisterAll(this)
        Bukkit.getScheduler().cancelTask(task)
    }

    /** Returns the given [rule] for [entity]. */
    public fun <T : Any> getEntityRule(entity: Entity, rule: RuleFunction<T>): RemoteServerRule<T>? =
        getEntityRule(entity, rule.index)

    /** Returns the given [ruleIndex] for [entity]. */
    public fun <T : Any> getEntityRule(entity: Entity, ruleIndex: Int): RemoteServerRule<T>? =
        entities.computeIfAbsent(entity) { RuleHolder() }.let { holder ->
            manager.entityRules.create(ruleIndex, holder)
        }

    /**
     * When an entity starts being shown to a player we
     * send its data along as well. 
     * This will also send data about needed entities
     * when the player logs in / changes worlds.
     */
    @EventHandler
    public fun onEntityShown(e: PlayerTrackEntityEvent) {
        val holder = entities[e.entity] ?: return
        val protocol = manager.getProtocolVersion(e.player) ?: return

        // Add a 1 tick delay, since the player doesn't actually register
        // the entity when beginning tracking, so on localhost the client 
        // fails to add the extra entity data.
        Bukkit.getScheduler().scheduleSyncDelayedTask(manager.plugin, {
            manager.sendPacket(e.player,
                ClientboundSetExtraEntityDataPacket(
                    e.entity.entityId,
                    holder.rules
                        // Only include rules that are available to this player!
                        .filter { manager.entityRules.isAvailable(it.key, protocol) }
                        .ifEmpty { null }
                        ?.mapValues { (_, rule) ->
                            { buffer -> (rule as RemoteServerRule<Any>).write(rule.value, buffer) }
                        } ?: return@scheduleSyncDelayedTask
                )
            )
        }, 1)
    }

    /**
     * Remove data stored for an entity when the entity
     * gets removed. This also happens indirectly because we
     * use a WeakHashMap.
     */
    @EventHandler
    public fun onEntityRemoved(e: EntityRemoveFromWorldEvent) {
        entities.remove(e.entity)
    }
}