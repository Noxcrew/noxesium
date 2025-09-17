package com.noxcrew.noxesium.paper.feature

import com.noxcrew.noxesium.api.NoxesiumReferences
import com.noxcrew.noxesium.paper.NoxesiumPaper
import com.noxcrew.noxesium.paper.api.event.NoxesiumPlayerRegisteredEvent
import com.noxcrew.packet.PacketHandler
import com.noxcrew.packet.PacketListener
import com.noxcrew.packet.sendPacket
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.chunk.status.ChunkStatus
import org.bukkit.craftbukkit.CraftChunk
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

/** Bundles block entity components into data packest properly. */
public class PaperBlockEntityModule : ListeningNoxesiumFeature(), PacketListener {
    override fun onRegister() {
        super.onRegister()
        NoxesiumPaper.packetApi.registerListener(this)
    }

    override fun onUnregister() {
        super.onUnregister()
        NoxesiumPaper.packetApi.unregisterListener(this)
    }

    /**
     * Re-send block data packets for all block entities already sent
     * to this player.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public fun onFinishHandshake(e: NoxesiumPlayerRegisteredEvent) {
        e.player.sentChunks.forEach { chunk ->
            val nmsChunk = (chunk as CraftChunk).getHandle(ChunkStatus.FULL) ?: return@forEach
            nmsChunk.blockEntities.values.forEach { blockEntity ->
                // Ignore block entities which do not have Noxesium data
                if (NoxesiumReferences.COMPONENT_NAMESPACE !in blockEntity.persistentDataContainer.raw) return@forEach

                // Re-send data for this specific block entity to the player!
                e.player.sendPacket(blockEntity.updatePacket)
            }
        }
    }

    /** Attaches block entity data to the data packet. */
    @PacketHandler
    public fun onBlockEntityPacket(player: Player, packet: ClientboundBlockEntityDataPacket): ClientboundBlockEntityDataPacket {
        val nmsWorld = (player.world as CraftWorld).handle
        val blockEntity = nmsWorld.getBlockEntityIfExists(packet.pos)
        if (blockEntity != null) {
            val noxesiumData =
                blockEntity.persistentDataContainer.raw[NoxesiumReferences.COMPONENT_NAMESPACE] as? CompoundTag ?: return packet
            packet.tag.put(NoxesiumReferences.COMPONENT_NAMESPACE, noxesiumData)
        }
        return packet
    }

    /** Attaches block entity data to the chunk packet. */
    @PacketHandler
    public fun onChunkDataPacket(player: Player, packet: ClientboundLevelChunkWithLightPacket): ClientboundLevelChunkWithLightPacket {
        val nmsWorld = (player.world as CraftWorld).handle

        // Process all block entities nested in the chunk
        packet.chunkData.getBlockEntitiesTagsConsumer(packet.x, packet.z).accept(BlockEntityDataStorer(nmsWorld))

        // Process all nested extra block entity packets
        packet.chunkData.extraPackets.forEach { subPacket ->
            if (subPacket is ClientboundBlockEntityDataPacket) {
                val blockEntity = nmsWorld.getBlockEntityIfExists(subPacket.pos)
                if (blockEntity != null) {
                    val noxesiumData =
                        blockEntity.persistentDataContainer.raw[NoxesiumReferences.COMPONENT_NAMESPACE] as? CompoundTag ?: return packet
                    subPacket.tag.put(NoxesiumReferences.COMPONENT_NAMESPACE, noxesiumData)
                }
            }
        }
        return packet
    }

    /** Adds Noxesium data to a block entity's data. */
    private inner class BlockEntityDataStorer(private val nmsWorld: ServerLevel) : ClientboundLevelChunkPacketData.BlockEntityTagOutput {
        override fun accept(pos: BlockPos, type: BlockEntityType<*>, tag: CompoundTag?,) {
            val blockEntity = nmsWorld.getBlockEntityIfExists(pos)
            if (blockEntity != null) {
                val noxesiumData = blockEntity.persistentDataContainer.raw[NoxesiumReferences.COMPONENT_NAMESPACE] as? CompoundTag ?: return
                tag?.put(NoxesiumReferences.COMPONENT_NAMESPACE, noxesiumData)
            }
        }
    }

    /** Returns the block entity at the given [blockPos]. */
    private fun ServerLevel.getBlockEntityIfExists(blockPos: BlockPos): BlockEntity? = getChunkAt(blockPos).blockEntities[blockPos]
}
