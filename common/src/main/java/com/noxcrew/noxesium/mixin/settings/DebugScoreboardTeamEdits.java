package com.noxcrew.noxesium.mixin.settings;

import com.noxcrew.noxesium.NoxesiumMod;
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
    public void onExceptionCaught(ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
        if (NoxesiumMod.getInstance().getConfig().debugScoreboardTeams) {
            NoxesiumMod.getInstance().getLogger().info("Received set player team packet, team method: {}, player action: {}, name: {}, players: [{}], parameters: {}", packet.getTeamAction(), packet.getPlayerAction(), packet.getName(), String.join(", ", packet.getPlayers()), packet.getParameters().isPresent() ? "present" : "null");
        }
    }
}
