package com.noxcrew.noxesium.core.fabric;

import com.noxcrew.noxesium.api.ClientNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.api.nms.serialization.HandshakePacketSerializers;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import com.noxcrew.noxesium.core.fabric.feature.entity.QibBehaviorModule;
import com.noxcrew.noxesium.core.fabric.feature.misc.SyncGuiScale;
import com.noxcrew.noxesium.core.fabric.feature.misc.TeamGlowHotkeys;
import com.noxcrew.noxesium.core.fabric.feature.sounds.NoxesiumSoundModule;
import com.noxcrew.noxesium.core.fabric.network.CommonComponentChangeListeners;
import com.noxcrew.noxesium.core.fabric.network.CommonPacketHandling;
import com.noxcrew.noxesium.core.network.CommonPackets;
import com.noxcrew.noxesium.core.nms.serialization.CommonBlockEntityComponentSerializers;
import com.noxcrew.noxesium.core.nms.serialization.CommonEntityComponentSerializers;
import com.noxcrew.noxesium.core.nms.serialization.CommonGameComponentSerializers;
import com.noxcrew.noxesium.core.nms.serialization.CommonItemComponentSerializers;
import com.noxcrew.noxesium.core.nms.serialization.CommonPacketSerializers;
import com.noxcrew.noxesium.core.nms.serialization.NmsGameComponentTypes;
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
 * Implements the common Noxesium entrypoint on Fabric.
 */
public class CommonFabricNoxesiumEntrypoint implements ClientNoxesiumEntrypoint {

    private final TeamGlowHotkeys teamGlowHotkeys = new TeamGlowHotkeys();
    private final QibBehaviorModule qibBehaviorModule = new QibBehaviorModule();
    private final CommonPacketHandling commonPacketHandling = new CommonPacketHandling();
    private final CommonComponentChangeListeners commonComponentChangeListeners = new CommonComponentChangeListeners();

    public CommonFabricNoxesiumEntrypoint() {
        CommonBlockEntityComponentSerializers.register();
        CommonEntityComponentSerializers.register();
        CommonGameComponentSerializers.register();
        CommonItemComponentSerializers.register();

        HandshakePacketSerializers.register();
        CommonPacketSerializers.register();
    }

    @Override
    public String getId() {
        return NoxesiumReferences.COMMON_ENTRYPOINT;
    }

    @Override
    public int getProtocolVersion() {
        return 19;
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
                NmsGameComponentTypes.INSTANCE);
    }

    @Override
    @Nullable
    public URL getEncryptionKey() {
        return CommonFabricNoxesiumEntrypoint.class.getClassLoader().getResource("common-encryption-key.aes");
    }
}
