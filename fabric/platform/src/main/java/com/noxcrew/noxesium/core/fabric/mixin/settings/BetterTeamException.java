package com.noxcrew.noxesium.core.fabric.mixin.settings;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Improves the error message thrown when a player is removed from the wrong team.
 */
@Mixin(Scoreboard.class)
public abstract class BetterTeamException {

    @Shadow
    @Nullable
    public abstract PlayerTeam getPlayersTeam(String string);

    @ModifyArg(
            method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V",
            at = @At(value = "INVOKE", target = "Ljava/lang/IllegalStateException;<init>(Ljava/lang/String;)V"),
            index = 0)
    public String onExceptionCaught(
            String ignored, @Local(argsOnly = true) String entity, @Local(argsOnly = true) PlayerTeam team) {
        var currentTeam = getPlayersTeam(entity);
        if (currentTeam == null) {
            return "Player '" + entity + "' is not on any team. Cannot remove from team '" + team.getName() + "'.";
        } else {
            return "Player '" + entity + "' is either on another team '" + currentTeam.getName()
                    + "'. Cannot remove from team '" + team.getName() + "'.";
        }
    }
}
