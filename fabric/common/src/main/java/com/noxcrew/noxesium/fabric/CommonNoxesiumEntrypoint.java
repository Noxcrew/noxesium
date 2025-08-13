package com.noxcrew.noxesium.fabric;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.fabric.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.fabric.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.fabric.network.PacketCollection;
import com.noxcrew.noxesium.fabric.feature.entity.QibBehaviorModule;
import com.noxcrew.noxesium.fabric.feature.entity.SpatialDebuggingRenderer;
import com.noxcrew.noxesium.fabric.feature.misc.CustomServerCreativeItems;
import com.noxcrew.noxesium.fabric.feature.misc.SyncGuiScale;
import com.noxcrew.noxesium.fabric.feature.misc.TeamGlowHotkeys;
import com.noxcrew.noxesium.fabric.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.fabric.feature.skull.SkullFontModule;
import com.noxcrew.noxesium.fabric.feature.sounds.NoxesiumSoundModule;
import com.noxcrew.noxesium.fabric.network.CommonPackets;
import com.noxcrew.noxesium.fabric.network.NoxesiumPacketHandling;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

/**
 * Implements the common Noxesium entrypoint.
 */
public class CommonNoxesiumEntrypoint implements NoxesiumEntrypoint {

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
        features.add(new ServerRuleModule());
        features.add(new SkullFontModule());
        features.add(new NoxesiumSoundModule());
        features.add(new TeamGlowHotkeys());
        features.add(new NoxesiumPacketHandling());
        features.add(new QibBehaviorModule());
        features.add(new SpatialDebuggingRenderer());
        features.add(new CustomServerCreativeItems());
        return features;
    }

    @Override
    public Collection<PacketCollection> getPacketCollections() {
        return List.of(CommonPackets.INSTANCE);
    }

    @Override
    @Nullable
    public URL getEncryptionKey() {
        return CommonNoxesiumEntrypoint.class.getClassLoader().getResource("common-encryption-key.aes");
    }
}
