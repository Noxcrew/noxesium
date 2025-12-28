package com.noxcrew.noxesium.paper.feature.qib

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import com.noxcrew.noxesium.core.nms.qib.QibCollisionManager
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes
import com.noxcrew.noxesium.paper.NoxesiumPaper
import com.noxcrew.noxesium.paper.api.event.NoxesiumEntityComponentChangedEvent
import com.noxcrew.noxesium.paper.api.event.NoxesiumEntityComponentsClearEvent
import com.noxcrew.noxesium.paper.api.event.NoxesiumPlayerAddedToWorldEvent
import com.noxcrew.noxesium.paper.api.event.NoxesiumPlayerUnregisteredEvent
import com.noxcrew.noxesium.paper.feature.ListeningNoxesiumFeature
import com.noxcrew.noxesium.paper.feature.hasNoxesiumComponent
import com.noxcrew.noxesium.paper.feature.noxesiumPlayer
import io.papermc.paper.event.entity.EntityMoveEvent
import net.minecraft.world.entity.Interaction
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.entity.EntityPortalExitEvent
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldUnloadEvent
import java.util.WeakHashMap

/** Add's server-side qib logic for non-Noxesium players. */
public class PaperQibModule : ListeningNoxesiumFeature() {
    private val containers = WeakHashMap<World, ServerSpatialInteractionEntityTree>()
    private val players = WeakHashMap<Player, QibCollisionManager>()

    override fun onRegister() {
        super.onRegister()

        // Rebuild containers that need rebuilding
        Bukkit.getScheduler().runTaskTimer(
            NoxesiumPaper.plugin,
            Runnable {
                for (container in containers.values) {
                    container.rebuild()
                }
            },
            100,
            100,
        )

        // Test for player locations every tick, checking against their previous position and
        // interpolating in between. This is not perfect, if there's lag we can miss a packet, but good enough!
        Bukkit.getScheduler().runTaskTimer(
            NoxesiumPaper.plugin,
            Runnable {
                players.values.forEach { it.tick() }
            },
            0,
            1,
        )

        // Go through all entities already in the worlds and add them to the containers
        Bukkit.getWorlds().forEach { world ->
            world.entities.forEach { entity ->
                if (entity.type != EntityType.INTERACTION) return@forEach
                if (!entity.hasNoxesiumComponent(CommonEntityComponentTypes.QIB_BEHAVIOR)) return@forEach
                val interaction = (entity as CraftEntity).handle as? Interaction ?: return@forEach
                getSpatialTree(entity).update(interaction)
            }
        }
    }

    /** Returns the spatial tree for the given [entity]. */
    private fun getSpatialTree(entity: Entity): ServerSpatialInteractionEntityTree =
        containers.computeIfAbsent(entity.world) { ServerSpatialInteractionEntityTree((entity.world as CraftWorld).handle) }

    /** Check if we should start checking when a player joins. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public fun onPlayerJoin(e: PlayerJoinEvent) {
        if (e.player.noxesiumPlayer != null) return
        players.computeIfAbsent(e.player) {
            ServerQibCollisionManager(
                (e.player as CraftPlayer).handle,
                getSpatialTree(e.player),
            )
        }
    }

    /** Update the player object when a player switches worlds. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public fun onPlayerJoin(e: PlayerChangedWorldEvent) {
        if (!players.contains(e.player)) return
        players.put(
            e.player,
            ServerQibCollisionManager(
                (e.player as CraftPlayer).handle,
                getSpatialTree(e.player),
            ),
        )
    }

    /** Check if we should start checking when a player unregisters. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public fun onPlayerUnregistered(e: NoxesiumPlayerUnregisteredEvent) {
        val onlinePlayer = Bukkit.getPlayer(e.noxesiumPlayer.uniqueId) ?: return
        players.computeIfAbsent(onlinePlayer) {
            ServerQibCollisionManager(
                (onlinePlayer as CraftPlayer).handle,
                getSpatialTree(onlinePlayer),
            )
        }
    }

    /** Clean up behaviors when a player registers with Noxesium. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public fun onPlayerRegistered(e: NoxesiumPlayerAddedToWorldEvent) {
        players.remove(e.player)
    }

    /** Clean up behaviors when a player quits. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public fun onPlayerQuit(e: PlayerQuitEvent) {
        players.remove(e.player)
    }

    /** Trigger jump behaviors when a player jumps. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public fun onPlayerJump(e: PlayerJumpEvent) {
        players[e.player]?.onPlayerJump()
    }

    /** Update positions whenever the entity moves. */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public fun onEntityMove(e: EntityMoveEvent) {
        if (e.entityType != EntityType.INTERACTION) return
        if (!e.hasExplicitlyChangedPosition()) return
        if (!e.entity.hasNoxesiumComponent(CommonEntityComponentTypes.QIB_BEHAVIOR)) return
        val interaction = (e.entity as CraftEntity).handle as? Interaction ?: return
        containers[e.entity.world]?.update(interaction)
    }

    // Update the position on various entity teleports.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public fun onEntityTeleport(e: EntityTeleportEvent): Unit = handleEntityTeleport(e.entity, e.from)

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public fun onEntityEnterPortal(e: EntityPortalEvent): Unit = handleEntityTeleport(e.entity, e.from)

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public fun onEntityExitPortal(e: EntityPortalExitEvent): Unit = handleEntityTeleport(e.entity, e.from)

    /** Handles [entity] teleporting. */
    private fun handleEntityTeleport(entity: Entity, location: Location) {
        if (entity.type != EntityType.INTERACTION) return
        if (!entity.hasNoxesiumComponent(CommonEntityComponentTypes.QIB_BEHAVIOR)) return
        val interaction = (entity as CraftEntity).handle as? Interaction ?: return
        if (entity.world !== location.world) {
            // Remove the entity from their previous world!
            containers[location.world]?.remove(interaction)
        }
        getSpatialTree(entity).update(interaction)
    }

    /** Update an entity's position when added. */
    @EventHandler(priority = EventPriority.HIGH)
    public fun onEntityAdded(e: EntityAddToWorldEvent) {
        if (e.entityType != EntityType.INTERACTION) return
        if (!e.entity.hasNoxesiumComponent(CommonEntityComponentTypes.QIB_BEHAVIOR)) return
        val interaction = (e.entity as CraftEntity).handle as? Interaction ?: return
        getSpatialTree(e.entity).update(interaction)
    }

    /** Remove an entity when removed from the world. */
    @EventHandler(priority = EventPriority.HIGH)
    public fun onEntityRemoved(e: EntityRemoveFromWorldEvent) {
        if (e.entityType != EntityType.INTERACTION) return
        if (!e.entity.hasNoxesiumComponent(CommonEntityComponentTypes.QIB_BEHAVIOR)) return
        val interaction = (e.entity as CraftEntity).handle as? Interaction ?: return
        containers[e.world]?.remove(interaction)
    }

    /** Remove a world's container when unloaded. */
    @EventHandler(priority = EventPriority.HIGH)
    public fun onWorldUnloaded(e: WorldUnloadEvent) {
        containers -= e.world
    }

    /** Update data if qib behavior is set. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public fun onEntityDataChanged(e: NoxesiumEntityComponentChangedEvent<*>) {
        if (e.entityType != EntityType.INTERACTION) return
        if (e.component != CommonEntityComponentTypes.QIB_BEHAVIOR) return

        val interaction = (e.entity as CraftEntity).handle as? Interaction ?: return
        if (e.newValue == null) {
            // If there isn't a value anymore, remove this entity!
            containers[e.entity.world]?.remove(interaction)
        } else if (e.oldValue == null) {
            // If there wasn't a value before, add this entity!
            getSpatialTree(e.entity).update(interaction)
        }
    }

    /** Remove data when cleared. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public fun onEntityDataCleared(e: NoxesiumEntityComponentsClearEvent) {
        if (e.entityType != EntityType.INTERACTION) return
        if (!e.entity.hasNoxesiumComponent(CommonEntityComponentTypes.QIB_BEHAVIOR)) return
        val interaction = (e.entity as CraftEntity).handle as? Interaction ?: return
        containers[e.entity.world]?.remove(interaction)
    }
}
