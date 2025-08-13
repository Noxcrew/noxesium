package com.noxcrew.noxesium.fabric.mixin.settings;

import com.noxcrew.noxesium.api.fabric.NoxesiumApi;
import com.noxcrew.noxesium.fabric.NoxesiumMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds debug messages to scoreboard team packets.
 */
@Mixin(ClientPacketListener.class)
public abstract class DebugScoreboardTeamEdits {

    @Inject(method = "handleSetPlayerTeamPacket", at = @At("HEAD"))
    public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
        if (Minecraft.getInstance().isSameThread() && NoxesiumMod.getInstance().getConfig().debugScoreboardTeams) {
            NoxesiumApi.getLogger()
                    .info(
                            "Received set player team packet, team method: {}, player action: {}, name: {}, players: [{}], parameters: {}",
                            packet.getTeamAction(),
                            packet.getPlayerAction(),
                            packet.getName(),
                            String.join(", ", packet.getPlayers()),
                            packet.getParameters().isPresent() ? "present" : "null");
        }
    }
}
