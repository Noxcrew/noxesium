package com.noxcrew.noxesium.paper.feature.qib

import com.noxcrew.noxesium.core.qib.SpatialTree
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Interaction
import net.minecraft.world.phys.AABB
import org.khelekore.prtree.PRTree
import org.khelekore.prtree.SimpleMBR
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Stores a spatial tree with the locations of all interaction entities.
 */
public class ServerSpatialInteractionEntityTree(
    private val level: ServerLevel,
) : SpatialTree {
    private companion object {
        private const val DEFAULT_BRANCHING_FACTOR = 30
    }

    private val pendingEntities = ConcurrentHashMap<Int, Entity>()
    private val removedEntities = ConcurrentHashMap.newKeySet<Int>()
    private val rebuilding = AtomicBoolean()
    private val needsRebuilding = AtomicBoolean()

    private var staticEntities = mutableSetOf<Int>()
    private var staticModel: PRTree<Entity> = PRTree(ServerEntityMBRConverter(), DEFAULT_BRANCHING_FACTOR)

    init {
        staticModel.load(emptyList())
    }

    /**
     * Rebuilds the model if applicable.
     */
    public fun rebuild() {
        if (!needsRebuilding.get()) return
        println("rebuild")
        if (!rebuilding.compareAndSet(false, true)) return
        try {
            val converter = ServerEntityMBRConverter()
            val newModel = PRTree(converter, DEFAULT_BRANCHING_FACTOR)

            val newStaticEntities = staticEntities.toMutableSet()
            val addedEntities = pendingEntities.keys.toSet()
            val removingEntities = removedEntities.toSet()
            needsRebuilding.set(false)

            newStaticEntities += addedEntities
            newStaticEntities -= removingEntities

            // Determine all actual entities that match the entity ids
            newModel.load(
                buildSet {
                    val iterator = newStaticEntities.iterator()
                    while (iterator.hasNext()) {
                        val id = iterator.next()
                        val entity = level.entities.get(id)
                        if (entity == null) {
                            iterator.remove()
                        } else {
                            add(entity)
                        }
                    }
                },
            )

            staticModel = newModel
            staticEntities = newStaticEntities
            pendingEntities.keys -= addedEntities
            removedEntities -= removingEntities
        } finally {
            rebuilding.set(false)
        }
    }

    override fun findEntities(hitbox: AABB): Set<Entity> = buildSet {
        val values = doubleArrayOf(hitbox.minX, hitbox.maxX, hitbox.minY, hitbox.maxY, hitbox.minZ, hitbox.maxZ)
        val mbr = SimpleMBR(*values)

        // Go through the model but ignoring removed entities
        if (removedEntities.isEmpty()) {
            for (entity in staticModel.find(mbr)) {
                add(entity)
            }
        } else {
            for (entity in staticModel.find(mbr)) {
                if (removedEntities.contains(entity.id)) continue
                add(entity)
            }
        }

        // Go through all pending entities that are not yet
        // in the model
        for (entity in pendingEntities.values) {
            if (entity.boundingBox.intersects(hitbox)) {
                add(entity)
            }
        }
    }

    /** Updates the current position of [entity]. */
    public fun update(entity: Interaction) {
        println("update $entity")
        if (entity.isRemoved) {
            remove(entity)
            return
        }

        removedEntities.remove(entity.id)
        if (entity.id !in staticEntities) {
            pendingEntities.put(entity.id, entity)
        }
        needsRebuilding.set(true)
    }

    /** Removes a given [entity] from the tree. */
    public fun remove(entity: Interaction) {
        pendingEntities.remove(entity.id)
        if (entity.id in staticEntities) {
            removedEntities.add(entity.id)
        }
        needsRebuilding.set(true)
    }

    /** Clears all stored information. */
    public fun clear() {
        pendingEntities.clear()
        removedEntities.addAll(staticEntities)
        needsRebuilding.set(true)
    }
}
