package com.noxcrew.noxesium.sync

import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundRequestSyncPacket

/**
 * Starts the folder syncing protocol with this player over the given
 * folder id. This will cause the client to receive a pop-up asking
 * them to confirm this sync and to pick a target directory.
 */
public fun NoxesiumServerPlayer.startFolderSync(folderId: String?) {
    sendPacket(ClientboundRequestSyncPacket(folderId))
}
