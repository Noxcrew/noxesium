package com.noxcrew.noxesium.sync;

import com.noxcrew.noxesium.api.ClientNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.sync.filesystem.FolderSyncSystem;
import com.noxcrew.noxesium.sync.network.SyncPacketSerializers;
import com.noxcrew.noxesium.sync.network.SyncPackets;
import java.util.Collection;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Sets up Noxesium Sync's entrypoint.
 */
public class NoxesiumSyncFabricEntrypoint implements ClientNoxesiumEntrypoint {

    private FolderSyncSystem folderSyncSystem;

    @Override
    public void preInitialize() {
        SyncPacketSerializers.register();
    }

    @Override
    public void initialize() {
        folderSyncSystem = new FolderSyncSystem();
    }

    @Override
    public String getVersion() {
        return FabricLoader.getInstance()
                .getModContainer("noxesium_sync")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    @Override
    public String getId() {
        return "noxesium-sync";
    }

    @Override
    public Collection<NoxesiumFeature> getAllFeatures() {
        return List.of(folderSyncSystem);
    }

    @Override
    public Collection<PacketCollection> getPacketCollections() {
        return List.of(SyncPackets.INSTANCE);
    }
}
