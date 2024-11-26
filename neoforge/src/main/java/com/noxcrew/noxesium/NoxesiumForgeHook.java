package com.noxcrew.noxesium;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.network.serverbound.ServerboundNoxesiumPacket;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements platform hooks on Fabric.
 */
public class NoxesiumForgeHook implements NoxesiumPlatformHook {

    private final ModContainer modContainer;
    private final Set<KeyMapping> keyMappings = new HashSet<>();

    public NoxesiumForgeHook(ModContainer modContainer) {
        this.modContainer = modContainer;
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isModLoaded(String modName) {
        return ModList.get().isLoaded(modName);
    }

    @Override
    public String getNoxesiumVersion() {
        return modContainer.getModInfo().getVersion().toString();
    }

    @Override
    public void registerTickEventHandler(Runnable runnable) {
        NeoForge.EVENT_BUS.<ClientTickEvent.Post>addListener((ignored) -> runnable.run());
    }

    @Override
    public void registerKeyBinding(KeyMapping keyMapping) {
        keyMappings.add(keyMapping);
    }

    @Override
    public void registerRenderHook(Runnable runnable) {
        NeoForge.EVENT_BUS.<RenderLevelStageEvent>addListener((event) -> {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                // If Fabulous mode is used we want to render earlier!
                if (Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FABULOUS) {
                    runnable.run();
                }
            } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
                // If not using Fabulous we render last
                if (Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
                    runnable.run();
                }
            }
        });
    }

    @SubscribeEvent
    public void registerBindings(RegisterKeyMappingsEvent event) {
        for (var mapping : keyMappings) {
            event.register(mapping);
        }
    }

    @Override
    public boolean canSend(NoxesiumPayloadType<?> type) {
        // TODO Implement!
        return false;
    }

    @Override
    public <T extends NoxesiumPacket> void registerPacket(NoxesiumPayloadType<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, boolean clientToServer) {
        // TODO Implement!
    }

    @Override
    public void sendPacket(ServerboundNoxesiumPacket packet) {
        PacketDistributor.sendToServer(packet);
    }

    @Override
    public <T extends CustomPacketPayload> void registerReceiver(CustomPacketPayload.Type<T> type, boolean global) {
        // TODO Implement!
    }
}
