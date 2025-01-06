package com.noxcrew.noxesium;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPacketHandler;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.network.serverbound.ServerboundNoxesiumPacket;
import java.nio.file.Path;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Implements platform hooks on Fabric.
 */
public class NoxesiumFabricHook implements NoxesiumPlatformHook {

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modName) {
        return FabricLoader.getInstance().isModLoaded(modName);
    }

    @Override
    public String getNoxesiumVersion() {
        return "fabric-"
                + FabricLoader.getInstance()
                        .getModContainer(NoxesiumReferences.NAMESPACE)
                        .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                        .orElse("unknown");
    }

    @Override
    public void registerTickEventHandler(Runnable runnable) {
        ClientTickEvents.END_CLIENT_TICK.register((ignored) -> runnable.run());
    }

    @Override
    public void registerRenderHook(Runnable runnable) {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> {
            // If Fabulous mode is used we want to render earlier!
            if (ctx.advancedTranslucency()) {
                runnable.run();
            }
        });

        WorldRenderEvents.LAST.register(ctx -> {
            // If not using Fabulous we render last
            if (!ctx.advancedTranslucency()) {
                runnable.run();
            }
        });
    }

    @Override
    public void registerKeyBinding(KeyMapping keyMapping) {
        KeyBindingHelper.registerKeyBinding(keyMapping);
    }

    @Override
    public boolean canSend(NoxesiumPayloadType<?> type) {
        return ClientPlayNetworking.canSend(type.type);
    }

    @Override
    public <T extends NoxesiumPacket> void registerPacket(
            NoxesiumPayloadType<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, boolean clientToServer) {
        if (clientToServer) {
            PayloadTypeRegistry.playC2S().register(type.type, codec);
        } else {
            PayloadTypeRegistry.playS2C().register(type.type, codec);
        }
    }

    @Override
    public void sendPacket(ServerboundNoxesiumPacket packet) {
        ClientPlayNetworking.send(packet);
    }

    @Override
    public <T extends CustomPacketPayload> void registerReceiver(CustomPacketPayload.Type<T> type, boolean global) {
        var handler = new NoxesiumPacketHandler<T>();
        if (global) {
            ClientPlayNetworking.registerGlobalReceiver(type, handler);
        } else {
            ClientPlayNetworking.registerReceiver(type, handler);
        }
    }
}
