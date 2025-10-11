package com.noxcrew.noxesium.network;

import static com.noxcrew.noxesium.api.util.ByteUtil.hasFlag;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.NoxesiumModule;
import com.noxcrew.noxesium.feature.entity.ExtraEntityDataModule;
import com.noxcrew.noxesium.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.feature.sounds.EntityNoxesiumSoundInstance;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundInstance;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundModule;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

/**
 * Registers default listeners for all base packets.
 */
public class NoxesiumPacketHandling implements NoxesiumModule {

    @Override
    public void onStartup() {
        NoxesiumPackets.CLIENT_SERVER_INFO.addListener(this, (reference, packet, context) -> {
            // Whenever the server sends information about the supported protocol version we store
            // that so we know what kind of packets to expect
            NoxesiumMod.getInstance().setServerVersion(packet.maxProtocolVersion());
        });

        NoxesiumPackets.CLIENT_RESET.addListener(this, (reference, packet, context) -> {
            var flags = packet.flags();
            if (hasFlag(flags, 0)) {
                NoxesiumMod.getInstance().getModule(ServerRuleModule.class).clearAll();
            }
        });

        NoxesiumPackets.CLIENT_CHANGE_SERVER_RULES.addListener(this, (reference, packet, context) -> {
            var indices = packet.indices();
            for (var idx = 0; idx < indices.size(); idx++) {
                var index = indices.getInt(idx);
                var rule = NoxesiumMod.getInstance()
                        .getModule(ServerRuleModule.class)
                        .getIndex(index);
                if (rule == null) return;
                rule.setUnsafe(packet.values().get(idx));
            }
        });

        NoxesiumPackets.CLIENT_RESET_SERVER_RULES.addListener(this, (reference, packet, context) -> {
            var module = NoxesiumMod.getInstance().getModule(ServerRuleModule.class);
            for (var index : packet.indices()) {
                var rule = module.getIndex(index);
                if (rule == null) continue;
                rule.reset();
            }
        });

        NoxesiumPackets.CLIENT_CUSTOM_SOUND_START.addListener(this, (reference, packet, context) -> {
            var manager = NoxesiumMod.getInstance().getModule(NoxesiumSoundModule.class);

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

        NoxesiumPackets.CLIENT_CUSTOM_SOUND_MODIFY.addListener(this, (reference, packet, context) -> {
            var manager = NoxesiumMod.getInstance().getModule(NoxesiumSoundModule.class);
            var sound = manager.getSound(packet.id());
            if (sound == null) return;
            sound.setVolume(packet.volume(), packet.startVolume(), packet.interpolationTicks());
        });

        NoxesiumPackets.CLIENT_CUSTOM_SOUND_STOP.addListener(this, (reference, packet, context) -> {
            var manager = NoxesiumMod.getInstance().getModule(NoxesiumSoundModule.class);
            manager.stopSound(packet.id());
        });

        NoxesiumPackets.CLIENT_CHANGE_EXTRA_ENTITY_DATA.addListener(this, (reference, packet, context) -> {
            Entity entity = context.player().level().getEntity(packet.entityId());
            if (entity != null) {
                var provider = NoxesiumMod.getInstance().getModule(ExtraEntityDataModule.class);
                var indices = packet.indices();
                for (var idx = 0; idx < indices.size(); idx++) {
                    var index = indices.getInt(idx);
                    var rule = provider.getIndex(index);
                    if (rule == null) return;
                    entity.noxesium$setExtraData(rule, packet.values().get(idx));
                }
            } else {
                NoxesiumMod.getInstance()
                        .getLogger()
                        .warn(
                                "Received ClientboundSetExtraEntityDataPacket about unknown entity {}",
                                packet.entityId());
            }
        });

        NoxesiumPackets.CLIENT_RESET_EXTRA_ENTITY_DATA.addListener(this, (reference, packet, context) -> {
            Entity entity = context.player().level().getEntity(packet.entityId());
            if (entity != null) {
                var provider = NoxesiumMod.getInstance().getModule(ExtraEntityDataModule.class);
                for (var index : packet.indices()) {
                    var rule = provider.getIndex(index);
                    if (rule == null) continue;
                    entity.noxesium$resetExtraData(rule);
                }
            } else {
                NoxesiumMod.getInstance()
                        .getLogger()
                        .warn(
                                "Received ClientboundResetExtraEntityDataPacket about unknown entity {}",
                                packet.entityId());
            }
        });

        NoxesiumPackets.CLIENT_OPEN_LINK.addListener(this, (reference, packet, context) -> {
            try {
                var uri = Util.parseAndValidateUntrustedUri(packet.url());
                var minecraft = Minecraft.getInstance();

                var text = packet.text() == null
                        ? Component.empty()
                        : packet.text().copy().append(CommonComponents.NEW_LINE).append(CommonComponents.NEW_LINE);

                text.append(Component.translatable("chat.link.confirmTrusted"));
                text.append(CommonComponents.NEW_LINE);
                text.append(CommonComponents.NEW_LINE);
                text.append(uri.toString());

                var screen = new ConfirmLinkScreen(
                        (result) -> {
                            if (result) Util.getPlatform().openUri(uri);
                            minecraft.setScreen(null);
                        },
                        Component.empty(), // Title, we dont use it because we override the whole message instead to
                        // allow for multiple lines
                        text,
                        uri,
                        CommonComponents.GUI_CANCEL,
                        true);
                minecraft.setScreen(screen);
            } catch (Exception e) {
                NoxesiumMod.getInstance().getLogger().warn("Failed to open link {}", packet.url(), e);
            }
        });
    }
}
