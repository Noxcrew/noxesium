package com.noxcrew.noxesium.paper.feature

import com.google.common.collect.HashMultimap
import com.noxcrew.noxesium.api.component.MutableNoxesiumComponentHolder
import com.noxcrew.noxesium.api.component.NoxesiumEntityManager
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.core.network.clientbound.ClientboundUpdateEntityComponentsPacket
import com.noxcrew.noxesium.paper.NoxesiumPaper
import com.noxcrew.noxesium.paper.api.event.NoxesiumPlayerRegisteredEvent
import com.noxcrew.noxesium.paper.component.EntityComponentHolder
import io.papermc.paper.event.player.PlayerTrackEntityEvent
import io.papermc.paper.event.player.PlayerUntrackEntityEvent
import net.minecraft.world.entity.Entity
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityRemoveEvent
import java.util.UUID

/** Loads in entities with the manager when they are created. */
public class PaperEntityModule : ListeningNoxesiumFeature() {
    /** A complete cache of all entities seen by all players. */
    private val seenEntities = HashMultimap.create<UUID, Int>()

    init {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            NoxesiumPaper.plugin,
            {
                if (!isRegistered) return@scheduleSyncRepeatingTask
                for (entity in NoxesiumEntityManager.getInstance<Entity, EntityComponentHolder>().allEntities) {
                    if (entity.hasModified()) {
                        val packet = ClientboundUpdateEntityComponentsPacket(entity.entity.id, false, entity.collectModified())

                        // Only if the entity is dirty do we determine which players are able to see it.
                        // We have to calculate a bit here as we don't store the final references to
                        // save on memory. We assume updates to the components are relatively rare (and
                        // we batch them), so we can afford this.
                        val entityId = entity.entity.id
                        val playerIds = seenEntities.keySet().filter { entityId in seenEntities[it] }
                        val playerManager = NoxesiumPlayerManager.getInstance()
                        playerIds.mapNotNull { playerManager.getPlayer(it) }.forEach { it.sendPacket(packet) }
                    }
                }
            },
            1, 1,
        )
    }

    /**
     * Send a newly registered player information about all entities they are already able
     * to see.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public fun onFinishHandshake(e: NoxesiumPlayerRegisteredEvent) {
        val noxesiumPlayer = e.noxesiumPlayer
        val lookup = (e.player.world as CraftWorld).handle.entities
        for (entityId in seenEntities.get(e.player.uniqueId)) {
            val entity = lookup.get(entityId) ?: continue
            val holder =
                NoxesiumEntityManager.getInstance<Entity, EntityComponentHolder>().getComponentHolderIfPresent(entity)
                    ?: continue
            noxesiumPlayer.sendPacket(
                ClientboundUpdateEntityComponentsPacket(
                    entity.id,
                    true,
                    holder.collectAll().takeUnless { it.isEmpty } ?: return,
                ),
            )
        }
    }

    /**
     * Evict data for any entities that get removed. We also store
     * entity data in a weak hashmap but this helps with cleaning up.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public fun onEntityRemoved(e: EntityRemoveEvent) {
        NoxesiumEntityManager.getInstance<Entity, MutableNoxesiumComponentHolder>().evictEntity((e.entity as CraftEntity).handle)
    }

    /**
     * Send data to a client whenever they are first tracking an entity.
     * The entity discards the data themselves when the entity is removed.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public fun onEntityShown(e: PlayerTrackEntityEvent) {
        // Track all entities seen by this player, even if they have not finished the handshake with Noxesium yet!
        seenEntities.put(e.player.uniqueId, e.entity.entityId)

        // Delay this by one task as the entity has not yet been sent, we need to send this packet
        // after the entity is known to the client!
        Bukkit.getScheduler().scheduleSyncDelayedTask(
            NoxesiumPaper.plugin,
            {
                val noxesiumPlayer = e.player.noxesiumPlayer ?: return@scheduleSyncDelayedTask
                val holder =
                    NoxesiumEntityManager
                        .getInstance<Entity, EntityComponentHolder>()
                        .getComponentHolderIfPresent((e.entity as CraftEntity).handle)
                        ?: return@scheduleSyncDelayedTask
                noxesiumPlayer.sendPacket(
                    ClientboundUpdateEntityComponentsPacket(
                        e.entity.entityId,
                        true,
                        holder.collectAll().takeUnless { it.isEmpty } ?: return@scheduleSyncDelayedTask,
                    ),
                )
            },
            1,
        )
    }

    /**
     * Keep track of when entities are untracked.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public fun onEntityHidden(e: PlayerUntrackEntityEvent) {
        seenEntities.remove(e.player.uniqueId, e.entity.entityId)
    }
}
