package com.noxcrew.noxesium.paper.entrypoint

import com.noxcrew.noxesium.api.NoxesiumEntrypoint
import com.noxcrew.noxesium.api.NoxesiumReferences
import com.noxcrew.noxesium.api.feature.NoxesiumFeature
import com.noxcrew.noxesium.api.network.PacketCollection
import com.noxcrew.noxesium.api.nms.serialization.HandshakePacketSerializers
import com.noxcrew.noxesium.api.registry.RegistryCollection
import com.noxcrew.noxesium.core.network.CommonPackets
import com.noxcrew.noxesium.core.nms.serialization.CommonBlockEntityComponentSerializers
import com.noxcrew.noxesium.core.nms.serialization.CommonEntityComponentSerializers
import com.noxcrew.noxesium.core.nms.serialization.CommonGameComponentSerializers
import com.noxcrew.noxesium.core.nms.serialization.CommonItemComponentSerializers
import com.noxcrew.noxesium.core.nms.serialization.CommonPacketSerializers
import com.noxcrew.noxesium.core.nms.serialization.NmsGameComponentTypes
import com.noxcrew.noxesium.core.registry.CommonBlockEntityComponentTypes
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes
import com.noxcrew.noxesium.core.registry.CommonItemComponentTypes
import com.noxcrew.noxesium.paper.feature.PaperBlockEntityModule
import com.noxcrew.noxesium.paper.feature.PaperEntityModule
import com.noxcrew.noxesium.paper.feature.RegistryLoader
import com.noxcrew.noxesium.paper.feature.SmoothTrident
import com.noxcrew.noxesium.paper.feature.qib.PaperQibModule
import java.net.URL

/**
 * Implements the common Noxesium entrypoint on Paper.
 */
public class CommonPaperNoxesiumEntrypoint : NoxesiumEntrypoint {
    init {
        CommonBlockEntityComponentSerializers.register()
        CommonEntityComponentSerializers.register()
        CommonGameComponentSerializers.register()
        CommonItemComponentSerializers.register()

        HandshakePacketSerializers.register()
        CommonPacketSerializers.register()
    }

    override fun getId(): String = NoxesiumReferences.COMMON_ENTRYPOINT

    override fun getPacketCollections(): Collection<PacketCollection> = listOf(
        CommonPackets.INSTANCE,
        CommonPackets.INSTANCE_CONFIG_COMPATIBLE,
    )

    override fun getRegistryCollections(): Collection<RegistryCollection<*>> = listOf(
        CommonBlockEntityComponentTypes.INSTANCE,
        CommonEntityComponentTypes.INSTANCE,
        CommonGameComponentTypes.INSTANCE,
        CommonItemComponentTypes.INSTANCE,
        NmsGameComponentTypes.INSTANCE,
    )

    override fun getAllFeatures(): Collection<NoxesiumFeature> = listOf(
        PaperEntityModule(),
        PaperBlockEntityModule(),
        RegistryLoader(),
        SmoothTrident(),
        PaperQibModule(),
    )

    override fun getEncryptionKey(): URL? =
        CommonPaperNoxesiumEntrypoint::class.java.getClassLoader().getResource("common-encryption-key.aes")
}
