package com.noxcrew.noxesium.sync

import com.noxcrew.noxesium.api.NoxesiumEntrypoint
import com.noxcrew.noxesium.api.feature.NoxesiumFeature
import com.noxcrew.noxesium.api.network.PacketCollection
import com.noxcrew.noxesium.sync.FolderSyncModule
import com.noxcrew.noxesium.sync.network.SyncPacketSerializers
import com.noxcrew.noxesium.sync.network.SyncPackets

/**
 * Implements the Paper entrypoint for Noxesium Sync.
 */
public class NoxesiumSyncPaperEntrypoint : NoxesiumEntrypoint {
    init {
        SyncPacketSerializers.register()
    }

    override fun getId(): String = "noxesium-sync"

    override fun getPacketCollections(): Collection<PacketCollection> = listOf(
        SyncPackets.INSTANCE,
    )

    override fun getAllFeatures(): Collection<NoxesiumFeature> = listOf(
        FolderSyncModule(),
    )
}
