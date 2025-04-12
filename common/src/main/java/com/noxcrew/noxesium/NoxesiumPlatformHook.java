package com.noxcrew.noxesium;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.network.serverbound.ServerboundNoxesiumPacket;
import java.nio.file.Path;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Provides various hooks with platform specific implementations.
 */
public interface NoxesiumPlatformHook {

    /**
     * Returns the configuration directory.
     */
    Path getConfigDirectory();

    /**
     * Returns whether a given mod name is loaded.
     */
    boolean isModLoaded(String modName);

    /**
     * Returns the version of Noxesium being used.
     */
    String getNoxesiumVersion();

    /**
     * Registers a new tick event handler.
     */
    void registerTickEventHandler(Runnable runnable);

    /**
     * Registers a new key mapping.
     */
    void registerKeyBinding(KeyMapping keyMapping);

    /**
     * Returns whether the given Noxesium payload can be sent.
     */
    boolean canSend(NoxesiumPayloadType<?> type);

    /**
     * Registers a new packet type.
     */
    <T extends NoxesiumPacket> void registerPacket(
            NoxesiumPayloadType<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, boolean clientToServer);

    /**
     * Sends the given packet to the server.
     */
    void sendPacket(ServerboundNoxesiumPacket packet);

    /**
     * Registers a new receiver.
     */
    <T extends CustomPacketPayload> void registerReceiver(CustomPacketPayload.Type<T> type, boolean global);
}
