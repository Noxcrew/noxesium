package com.noxcrew.noxesium.fabric.network;

import com.noxcrew.noxesium.api.fabric.NoxesiumApi;
import com.noxcrew.noxesium.api.fabric.feature.NoxesiumFeature;
import com.noxcrew.noxesium.fabric.feature.sounds.EntityNoxesiumSoundInstance;
import com.noxcrew.noxesium.fabric.feature.sounds.NoxesiumSoundInstance;
import com.noxcrew.noxesium.fabric.feature.sounds.NoxesiumSoundModule;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Registers default listeners for all base packets.
 */
public class NoxesiumPacketHandling implements NoxesiumFeature {

    @Override
    public void onRegister() {
        CommonPackets.INSTANCE.CLIENT_UPDATE_COMPONENTS.addListener(this, (reference, packet, context) -> {
            // No entity id being specified refers to the game itself, otherwise find the
            // relevant entity that the packet is referencing.
            var target = packet.entityId();
            if (target.isPresent()) {
                packet.patch().apply(Minecraft.getInstance());
            } else {
                var entity = context.player().clientLevel.getEntity(target.get());
                if (entity == null) {
                    NoxesiumApi.getLogger().warn("Received components for unknown entity {}", packet.entityId());
                } else {
                    packet.patch().apply(entity);
                }
            }
        });

        CommonPackets.INSTANCE.CLIENT_CUSTOM_SOUND_START.addListener(this, (reference, packet, context) -> {
            var manager = NoxesiumApi.getInstance().getFeature(NoxesiumSoundModule.class);

            // Determine the sound instance to play
            NoxesiumSoundInstance sound = null;
            if (packet.position() != null) {
                sound = new NoxesiumSoundInstance(
                        packet.sound(),
                        packet.source(),
                        packet.position(),
                        packet.volume(),
                        packet.pitch(),
                        packet.looping(),
                        packet.attenuation(),
                        packet.determineOffset());
            } else if (packet.entityId() != null) {
                var entity = context.player().connection.getLevel().getEntity(packet.entityId());
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

        CommonPackets.INSTANCE.CLIENT_CUSTOM_SOUND_MODIFY.addListener(this, (reference, packet, context) -> {
            var manager = NoxesiumApi.getInstance().getFeature(NoxesiumSoundModule.class);
            var sound = manager.getSound(packet.id());
            if (sound == null) return;
            sound.setVolume(packet.volume(), packet.startVolume(), packet.interpolationTicks());
        });

        CommonPackets.INSTANCE.CLIENT_CUSTOM_SOUND_STOP.addListener(this, (reference, packet, context) -> {
            var manager = NoxesiumApi.getInstance().getFeature(NoxesiumSoundModule.class);
            manager.stopSound(packet.id());
        });

        CommonPackets.INSTANCE.CLIENT_OPEN_LINK.addListener(this, (reference, packet, context) -> {
            try {
                var uri = Util.parseAndValidateUntrustedUri(packet.url());
                var minecraft = Minecraft.getInstance();
                var text = packet.text()
                        .orElse(Component.empty())
                        .copy()
                        .append(CommonComponents.NEW_LINE)
                        .append(CommonComponents.NEW_LINE);

                text.append(Component.translatable("chat.link.confirmTrusted"));
                text.append(CommonComponents.NEW_LINE);
                text.append(CommonComponents.NEW_LINE);
                text.append(uri.toString());

                // Title is empty since we don't use it because we override the whole message instead to allow for
                // multiple lines
                var screen = new ConfirmLinkScreen(
                        (result) -> {
                            if (result) Util.getPlatform().openUri(uri);
                            minecraft.setScreen(null);
                        },
                        Component.empty(),
                        text,
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
