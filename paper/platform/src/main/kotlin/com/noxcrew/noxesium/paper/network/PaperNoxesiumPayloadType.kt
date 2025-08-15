package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.network.NoxesiumPacket
import com.noxcrew.noxesium.api.nms.network.payload.NoxesiumPayloadType
import com.noxcrew.noxesium.paper.NoxesiumPaper
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import org.bukkit.Bukkit

/**
 * A paper-sided implementation of the payload type.
 */
public class PaperNoxesiumPayloadType<T : NoxesiumPacket>(
    key: ResourceLocation,
    codec: StreamCodec<RegistryFriendlyByteBuf, T>,
    clazz: Class<T>,
    clientToServer: Boolean,
) : NoxesiumPayloadType<T>(key, codec, clazz, clientToServer) {

    private val listener = PaperPacketHandler(this)

    override fun register() {
        super.register()

        if (clientToServer) {
            Bukkit.getMessenger().registerIncomingPluginChannel(NoxesiumPaper.plugin, id().toString(), listener)
        } else {
            Bukkit.getMessenger().registerOutgoingPluginChannel(NoxesiumPaper.plugin, id().toString())
        }
    }

    override fun unregister() {
        super.unregister()

        if (clientToServer) {
            Bukkit.getMessenger().unregisterIncomingPluginChannel(NoxesiumPaper.plugin, id().toString(), listener)
        } else {
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(NoxesiumPaper.plugin, id().toString())
        }
    }
}