package com.noxcrew.noxesium.paper.entrypoint

import com.noxcrew.noxesium.api.nms.NmsNoxesiumEntrypoint
import com.noxcrew.noxesium.api.nms.network.PacketCollection
import com.noxcrew.noxesium.api.registry.RegistryCollection
import com.noxcrew.noxesium.core.nms.network.CommonPackets
import com.noxcrew.noxesium.core.nms.registry.CommonBlockEntityComponentSerializers
import com.noxcrew.noxesium.core.nms.registry.CommonEntityComponentSerializers
import com.noxcrew.noxesium.core.nms.registry.CommonGameComponentSerializers
import com.noxcrew.noxesium.core.nms.registry.CommonItemComponentSerializers
import com.noxcrew.noxesium.core.nms.registry.NmsGameComponentTypes
import com.noxcrew.noxesium.core.registry.CommonBlockEntityComponentTypes
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes
import com.noxcrew.noxesium.core.registry.CommonItemComponentTypes
import java.net.URL

/**
 * Implements the common Noxesium entrypoint on Paper.
 */
public class CommonPaperNoxesiumEntrypoint : NmsNoxesiumEntrypoint {
    init {
        CommonBlockEntityComponentSerializers.register()
        CommonEntityComponentSerializers.register()
        CommonGameComponentSerializers.register()
        CommonItemComponentSerializers.register()
    }

    override fun getId(): String = "noxesium-common"

    override fun getPacketCollections(): Collection<PacketCollection> = listOf(
        CommonPackets.INSTANCE,
    )

    override fun getRegistryCollections(): Collection<RegistryCollection<*>> = listOf(
        CommonBlockEntityComponentTypes.INSTANCE,
        CommonEntityComponentTypes.INSTANCE,
        CommonGameComponentTypes.INSTANCE,
        CommonItemComponentTypes.INSTANCE,
        NmsGameComponentTypes.INSTANCE,
    )

    override fun getEncryptionKey(): URL? =
        CommonPaperNoxesiumEntrypoint::class.java.getClassLoader().getResource("common-encryption-key.aes")
}
