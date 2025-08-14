package com.noxcrew.noxesium.core.fabric;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.fabric.FabricNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.fabric.network.PacketCollection;
import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import com.noxcrew.noxesium.core.fabric.feature.entity.QibBehaviorModule;
import com.noxcrew.noxesium.core.fabric.feature.misc.SyncGuiScale;
import com.noxcrew.noxesium.core.fabric.feature.misc.TeamGlowHotkeys;
import com.noxcrew.noxesium.core.fabric.feature.sounds.NoxesiumSoundModule;
import com.noxcrew.noxesium.core.fabric.network.CommonPacketHandling;
import com.noxcrew.noxesium.core.fabric.network.CommonPackets;
import com.noxcrew.noxesium.core.fabric.registry.CommonBlockEntityComponentSerializers;
import com.noxcrew.noxesium.core.fabric.registry.CommonComponentChangeListeners;
import com.noxcrew.noxesium.core.fabric.registry.CommonEntityComponentSerializers;
import com.noxcrew.noxesium.core.fabric.registry.CommonGameComponentSerializers;
import com.noxcrew.noxesium.core.fabric.registry.CommonItemComponentSerializers;
import com.noxcrew.noxesium.core.fabric.registry.FabricGameComponentTypes;
import com.noxcrew.noxesium.core.registry.CommonBlockEntityComponentTypes;
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import com.noxcrew.noxesium.core.registry.CommonItemComponentTypes;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

/**
 * Implements the common Noxesium entrypoint.
 */
public class CommonNoxesiumEntrypoint implements FabricNoxesiumEntrypoint {

    private final TeamGlowHotkeys teamGlowHotkeys = new TeamGlowHotkeys();
    private final QibBehaviorModule qibBehaviorModule = new QibBehaviorModule();
    private final CommonPacketHandling commonPacketHandling = new CommonPacketHandling();
    private final CommonComponentChangeListeners commonComponentChangeListeners = new CommonComponentChangeListeners();

    public CommonNoxesiumEntrypoint() {
        CommonBlockEntityComponentSerializers.register();
        CommonEntityComponentSerializers.register();
        CommonGameComponentSerializers.register();
        CommonItemComponentSerializers.register();
    }

    @Override
    public String getId() {
        return "noxesium-common";
    }

    @Override
    public int getProtocolVersion() {
        return 100;
    }

    @Override
    public String getRawVersion() {
        return FabricLoader.getInstance()
                .getModContainer(NoxesiumReferences.NAMESPACE)
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    @Override
    public Collection<NoxesiumFeature> getAllFeatures() {
        var features = new ArrayList<NoxesiumFeature>();
        features.add(new SyncGuiScale());
        features.add(new NoxesiumSoundModule());
        features.add(commonComponentChangeListeners);
        features.add(commonPacketHandling);
        features.add(qibBehaviorModule);
        features.add(teamGlowHotkeys);
        return features;
    }

    @Override
    public Collection<PacketCollection> getPacketCollections() {
        return List.of(CommonPackets.INSTANCE);
    }

    @Override
    public Collection<RegistryCollection<?>> getRegistryCollections() {
        return List.of(
                CommonBlockEntityComponentTypes.INSTANCE,
                CommonEntityComponentTypes.INSTANCE,
                CommonGameComponentTypes.INSTANCE,
                CommonItemComponentTypes.INSTANCE,
                FabricGameComponentTypes.INSTANCE);
    }

    @Override
    @Nullable
    public URL getEncryptionKey() {
        return CommonNoxesiumEntrypoint.class.getClassLoader().getResource("common-encryption-key.aes");
    }
}
