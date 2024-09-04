package com.noxcrew.noxesium.paper.api.network.serverbound

import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.item.ItemStack
import org.bukkit.entity.Player

/**
 * Sent to the server whenever a player uses a riptide trident.
 */
public class ServerboundRiptidePacket(
    public val item: ItemStack,
) : ServerboundNoxesiumPacket(NoxesiumPackets.SERVER_RIPTIDE) {

    public constructor(buffer: FriendlyByteBuf, player: Player, protocolVersion: Int) : this(buffer.readJsonWithCodec(ItemStack.CODEC))
}
