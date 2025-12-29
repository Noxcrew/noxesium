package com.noxcrew.noxesium.core.fabric.network;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.component.NoxesiumComponentPatch;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.component.RemoteNoxesiumComponentHolder;
import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.nms.serialization.ComponentSerializerRegistry;
import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import com.noxcrew.noxesium.core.fabric.feature.sound.EntityNoxesiumSoundInstance;
import com.noxcrew.noxesium.core.fabric.feature.sound.NoxesiumSoundInstance;
import com.noxcrew.noxesium.core.fabric.feature.sound.NoxesiumSoundModule;
import com.noxcrew.noxesium.core.network.CommonPackets;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundModifyPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundStartPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundStopPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundOpenLinkPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundStopGlidePacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundUpdateEntityComponentsPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundUpdateGameComponentsPacket;
import java.util.List;
import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.Util;

/**
 * Registers default listeners for all base packets.
 */
public class CommonPacketHandling extends NoxesiumFeature {

    public CommonPacketHandling() {
        CommonPackets.CLIENT_UPDATE_GAME_COMPONENTS.addListener(
                this, ClientboundUpdateGameComponentsPacket.class, (reference, packet, ignored3) -> {
                    if (!reference.isRegistered()) return;
                    reference.applyPatch(
                            packet.patch(),
                            packet.reset(),
                            NoxesiumRegistries.GAME_COMPONENTS,
                            GameComponents.getInstance());
                });
        CommonPackets.CLIENT_UPDATE_ENTITY_COMPONENTS.addListener(
                this, ClientboundUpdateEntityComponentsPacket.class, (reference, packet, ignored3) -> {
                    if (!reference.isRegistered()) return;
                    if (Minecraft.getInstance().level == null) {
                        NoxesiumApi.getLogger()
                                .warn("Received components for entity {} when level is not set", packet.entityId());
                        return;
                    }
                    var entity = Minecraft.getInstance().level.getEntity(packet.entityId());
                    if (entity == null) {
                        NoxesiumApi.getLogger().warn("Received components for unknown entity {}", packet.entityId());
                    } else {
                        reference.applyPatch(
                                packet.patch(), packet.reset(), NoxesiumRegistries.ENTITY_COMPONENTS, entity);
                    }
                });

        CommonPackets.CLIENT_CUSTOM_SOUND_START.addListener(
                this, ClientboundCustomSoundStartPacket.class, (reference, packet, ignored3) -> {
                    if (!reference.isRegistered()) return;
                    var manager = NoxesiumApi.getInstance().getFeatureOrNull(NoxesiumSoundModule.class);
                    if (manager == null) return;

                    // Determine the sound instance to play
                    NoxesiumSoundInstance sound = null;
                    if (packet.position().isPresent()) {
                        sound = new NoxesiumSoundInstance(
                                packet.sound(),
                                packet.source(),
                                packet.position().get(),
                                packet.volume(),
                                packet.pitch(),
                                packet.offset(),
                                packet.looping(),
                                packet.attenuation());
                    } else if (packet.entityId().isPresent()) {
                        var entity = Minecraft.getInstance()
                                .player
                                .connection
                                .getLevel()
                                .getEntity(packet.entityId().get());
                        if (entity != null) {
                            sound = new EntityNoxesiumSoundInstance(
                                    packet.sound(),
                                    packet.source(),
                                    entity,
                                    packet.volume(),
                                    packet.pitch(),
                                    packet.offset(),
                                    packet.looping(),
                                    packet.attenuation(),
                                    false);
                        }
                    }
                    if (sound == null) {
                        sound = new EntityNoxesiumSoundInstance(
                                packet.sound(),
                                packet.source(),
                                Minecraft.getInstance().player,
                                packet.volume(),
                                packet.pitch(),
                                packet.offset(),
                                packet.looping(),
                                packet.attenuation(),
                                true);
                    }
                    manager.play(packet.id(), sound, packet.ignoreIfPlaying());
                });

        CommonPackets.CLIENT_CUSTOM_SOUND_MODIFY.addListener(
                this, ClientboundCustomSoundModifyPacket.class, (reference, packet, ignored3) -> {
                    if (!reference.isRegistered()) return;
                    var manager = NoxesiumApi.getInstance().getFeatureOrNull(NoxesiumSoundModule.class);
                    if (manager == null) return;
                    var sound = manager.getSound(packet.id());
                    if (sound == null) return;
                    sound.setVolume(packet.volume(), packet.startVolume(), packet.interpolationTicks());
                });

        CommonPackets.CLIENT_CUSTOM_SOUND_STOP.addListener(
                this, ClientboundCustomSoundStopPacket.class, (reference, packet, ignored3) -> {
                    if (!reference.isRegistered()) return;
                    var manager = NoxesiumApi.getInstance().getFeatureOrNull(NoxesiumSoundModule.class);
                    if (manager == null) return;
                    manager.stopSound(packet.id());
                });

        CommonPackets.CLIENT_OPEN_LINK.addListener(
                this, ClientboundOpenLinkPacket.class, (reference, packet, ignored3) -> {
                    if (!reference.isRegistered()) return;
                    try {
                        var uri = Util.parseAndValidateUntrustedUri(packet.url());
                        var minecraft = Minecraft.getInstance();
                        var text = Component.join(
                                JoinConfiguration.noSeparators(),
                                List.of(
                                        packet.text().orElse(Component.empty()),
                                        Component.newline(),
                                        Component.newline(),
                                        Component.translatable("chat.link.confirmTrusted"),
                                        Component.newline(),
                                        Component.newline(),
                                        Component.text(uri.toString())));

                        // Title is empty since we don't use it because we override the whole message instead to allow
                        // for multiple lines
                        var screen = new ConfirmLinkScreen(
                                (result) -> {
                                    if (result) Util.getPlatform().openUri(uri);
                                    minecraft.setScreen(null);
                                },
                                net.minecraft.network.chat.Component.empty(),
                                NonWrappingComponentSerializer.INSTANCE.serialize(text),
                                uri,
                                CommonComponents.GUI_CANCEL,
                                true);
                        minecraft.setScreen(screen);
                    } catch (Exception e) {
                        NoxesiumApi.getLogger().warn("Failed to open link {}", packet.url(), e);
                    }
                });

        CommonPackets.CLIENT_STOP_GLIDE.addListener(
                this, ClientboundStopGlidePacket.class, (reference, ignored2, ignored3) -> {
                    if (!reference.isRegistered()) return;
                    var player = Minecraft.getInstance().player;
                    if (player == null) return;
                    player.noxesium$stopFallFlying();
                });
    }

    /**
     * Applies a Noxesium component patch to the given holder.
     */
    private void applyPatch(
            NoxesiumComponentPatch patch,
            boolean reset,
            NoxesiumRegistry<NoxesiumComponentType<?>> registry,
            RemoteNoxesiumComponentHolder holder) {
        // Clear the values if a reset was requested
        if (reset) {
            holder.noxesium$reloadComponents();
        }

        // Apply the patch itself
        for (var entry : patch.getMap().entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var serializer = ComponentSerializerRegistry.getSerializers(registry, key);
            if (value.isEmpty()) {
                if (serializer != null
                        && serializer.listener() != null
                        && serializer.listener().hasListeners()) {
                    var oldValue = holder.noxesium$getComponent(key);
                    holder.noxesium$unsetComponent(key);
                    serializer.listener().trigger(holder, oldValue, null);
                } else {
                    holder.noxesium$unsetComponent(key);
                }
            } else {
                if (serializer != null
                        && serializer.listener() != null
                        && serializer.listener().hasListeners()) {
                    var oldValue = holder.noxesium$getComponent(key);
                    var newValue = value.orElse(null);
                    holder.noxesium$loadComponent(key, newValue);
                    serializer.listener().trigger(holder, oldValue, newValue);
                } else {
                    holder.noxesium$loadComponent(key, value.get());
                }
            }
        }
    }
}
