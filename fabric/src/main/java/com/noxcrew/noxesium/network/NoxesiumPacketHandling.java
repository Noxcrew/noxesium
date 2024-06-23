package com.noxcrew.noxesium.network;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.NoxesiumModule;
import com.noxcrew.noxesium.feature.OverrideChunkUpdates;
import com.noxcrew.noxesium.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.feature.skull.SkullFontModule;
import com.noxcrew.noxesium.feature.sounds.EntityNoxesiumSoundInstance;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundInstance;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundModule;

import static com.noxcrew.noxesium.api.util.ByteUtil.hasFlag;

/**
 * Registers default listeners for all base packets.
 */
public class NoxesiumPacketHandling implements NoxesiumModule {

    @Override
    public void onStartup() {
        NoxesiumPackets.SERVER_INFO.addListener(this, (reference, packet, context) -> {
            // Whenever the server sends information about the supported protocol version we store
            // that so we know what kind of packets to expect
            NoxesiumMod.getInstance().setServerVersion(packet.maxProtocolVersion());
        });

        NoxesiumPackets.RESET.addListener(this, (reference, packet, context) -> {
            var flags = packet.flags();
            if (hasFlag(flags, 0)) {
                NoxesiumMod.getInstance().getModule(ServerRuleModule.class).clearAll();
            }
            if (hasFlag(flags, 1)) {
                NoxesiumMod.getInstance().getModule(SkullFontModule.class).resetCaches();
            }
        });

        NoxesiumPackets.CHANGE_SERVER_RULES.addListener(this, (reference, packet, context) -> {
            var indices = packet.indices();
            for (var idx = 0; idx < indices.size(); idx++) {
                var index = indices.getInt(idx);
                var rule = NoxesiumMod.getInstance().getModule(ServerRuleModule.class).getIndex(index);
                if (rule == null) return;
                rule.setUnsafe(packet.values().get(idx));
            }
        });

        NoxesiumPackets.RESET_SERVER_RULES.addListener(this, (reference, packet, context) -> {
            var module = NoxesiumMod.getInstance().getModule(ServerRuleModule.class);
            for (var index : packet.indices()) {
                var rule = module.getIndex(index);
                if (rule == null) continue;
                rule.reset();
            }
        });

        NoxesiumPackets.CUSTOM_SOUND_START.addListener(this, (reference, packet, context) -> {
            var manager = NoxesiumMod.getInstance().getModule(NoxesiumSoundModule.class);

            // Determine the sound instance to play
            NoxesiumSoundInstance sound = null;
            if (packet.position() != null) {
                sound = new NoxesiumSoundInstance(packet.sound(), packet.source(), packet.position(), packet.volume(), packet.pitch(), packet.looping(), packet.attenuation(), packet.determineOffset());
            } else if (packet.entityId() != null) {
                var entity = context.player().connection.getLevel().getEntity(packet.entityId());
                if (entity != null) {
                    sound = new EntityNoxesiumSoundInstance(packet.sound(), packet.source(), entity, packet.volume(), packet.pitch(), packet.looping(), packet.attenuation(), packet.determineOffset());
                }
            }
            if (sound == null) {
                sound = new EntityNoxesiumSoundInstance(packet.sound(), packet.source(), context.player(), packet.volume(), packet.pitch(), packet.looping(), packet.attenuation(), packet.determineOffset());
            }
            manager.play(packet.id(), sound, packet.ignoreIfPlaying());
        });

        NoxesiumPackets.CUSTOM_SOUND_MODIFY.addListener(this, (reference, packet, context) -> {
            var manager = NoxesiumMod.getInstance().getModule(NoxesiumSoundModule.class);
            var sound = manager.getSound(packet.id());
            if (sound == null) return;
            sound.setVolume(packet.volume(), packet.startVolume(), packet.interpolationTicks());
        });

        NoxesiumPackets.CUSTOM_SOUND_STOP.addListener(this, (reference, packet, context) -> {
            var manager = NoxesiumMod.getInstance().getModule(NoxesiumSoundModule.class);
            manager.stopSound(packet.id());
        });

        NoxesiumPackets.MCC_SERVER.addListener(this, (reference, packet, context) -> {
            // If we join a server running Hole in the Wall we need to disable the Always Defer Chunk Updates setting!
            NoxesiumMod.getInstance().getModule(OverrideChunkUpdates.class).updateState(packet.associatedGame().equals("hole_in_the_wall"));
        });
    }
}
