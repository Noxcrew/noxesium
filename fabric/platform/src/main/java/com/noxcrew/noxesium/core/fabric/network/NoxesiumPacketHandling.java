package com.noxcrew.noxesium.core.fabric.network;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.core.fabric.feature.sounds.EntityNoxesiumSoundInstance;
import com.noxcrew.noxesium.core.fabric.feature.sounds.NoxesiumSoundInstance;
import com.noxcrew.noxesium.core.fabric.feature.sounds.NoxesiumSoundModule;
import java.util.List;
import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.network.chat.CommonComponents;

/**
 * Registers default listeners for all base packets.
 */
public class NoxesiumPacketHandling extends NoxesiumFeature {

    public NoxesiumPacketHandling() {
        CommonPackets.CLIENT_UPDATE_GAME_COMPONENTS.addListener(this, (reference, packet, context) -> {
            if (!isRegistered()) return;
            packet.patch().apply(Minecraft.getInstance());
        });
        CommonPackets.CLIENT_UPDATE_ENTITY_COMPONENTS.addListener(this, (reference, packet, context) -> {
            if (!isRegistered()) return;
            var entity = context.player().clientLevel.getEntity(packet.entityId());
            if (entity == null) {
                NoxesiumApi.getLogger().warn("Received components for unknown entity {}", packet.entityId());
            } else {
                packet.patch().apply(entity);
            }
        });

        CommonPackets.CLIENT_CUSTOM_SOUND_START.addListener(this, (reference, packet, context) -> {
            if (!isRegistered()) return;
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
                        packet.looping(),
                        packet.attenuation(),
                        packet.determineOffset());
            } else if (packet.entityId().isPresent()) {
                var entity = context.player()
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
                            packet.looping(),
                            packet.attenuation(),
                            packet.determineOffset());
                }
            }
            if (sound == null) {
                sound = new EntityNoxesiumSoundInstance(
                        packet.sound(),
                        packet.source(),
                        context.player(),
                        packet.volume(),
                        packet.pitch(),
                        packet.looping(),
                        packet.attenuation(),
                        packet.determineOffset());
            }
            manager.play(packet.id(), sound, packet.ignoreIfPlaying());
        });

        CommonPackets.CLIENT_CUSTOM_SOUND_MODIFY.addListener(this, (reference, packet, context) -> {
            if (!isRegistered()) return;
            var manager = NoxesiumApi.getInstance().getFeatureOrNull(NoxesiumSoundModule.class);
            if (manager == null) return;
            var sound = manager.getSound(packet.id());
            if (sound == null) return;
            sound.setVolume(packet.volume(), packet.startVolume(), packet.interpolationTicks());
        });

        CommonPackets.CLIENT_CUSTOM_SOUND_STOP.addListener(this, (reference, packet, context) -> {
            if (!isRegistered()) return;
            var manager = NoxesiumApi.getInstance().getFeatureOrNull(NoxesiumSoundModule.class);
            if (manager == null) return;
            manager.stopSound(packet.id());
        });

        CommonPackets.CLIENT_OPEN_LINK.addListener(this, (reference, packet, context) -> {
            if (!isRegistered()) return;
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

                // Title is empty since we don't use it because we override the whole message instead to allow for
                // multiple lines
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
    }
}
