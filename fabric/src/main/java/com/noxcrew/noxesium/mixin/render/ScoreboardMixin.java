package com.noxcrew.noxesium.mixin.render;

import com.noxcrew.noxesium.feature.render.CachedScoreboardContents;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Listens to changes to the scoreboard relevant to the caches.
 */
@Mixin(Scoreboard.class)
public class ScoreboardMixin {

    @Inject(method = "addPlayerToTeam", at = @At(value = "TAIL"))
    private void addPlayerToTeam(String string, PlayerTeam playerTeam, CallbackInfoReturnable<Boolean> cir) {
        if (CachedScoreboardContents.isPlayerRelevant(string)) {
            CachedScoreboardContents.clearCache();
        }
    }

    @Inject(method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V", at = @At(value = "TAIL"))
    private void removePlayerFromTeam(String string, PlayerTeam playerTeam, CallbackInfo ci) {
        if (CachedScoreboardContents.isPlayerRelevant(string)) {
            CachedScoreboardContents.clearCache();
        }
    }

    @Inject(method = "removePlayerTeam", at = @At(value = "TAIL"))
    private void removePlayerTeam(PlayerTeam playerTeam, CallbackInfo ci) {
        if (CachedScoreboardContents.isTeamRelevant(playerTeam.getName())) {
            CachedScoreboardContents.clearCache();
        }
    }

    @Inject(method = "setDisplayObjective", at = @At(value = "TAIL"))
    private void setDisplayObjective(DisplaySlot displaySlot, Objective objective, CallbackInfo ci) {
        // We don't need to care about changes to the below name or list slots.
        if (displaySlot == DisplaySlot.BELOW_NAME || displaySlot == DisplaySlot.LIST) return;

        // We do listen to any change to a team slot as the player could be seeing one
        // of those and notice it get overridden.
        CachedScoreboardContents.clearCache();
    }

    @Inject(method = "getOrCreatePlayerScore", at = @At(value = "TAIL"))
    private void getOrCreatePlayerScore(String string, Objective objective, CallbackInfoReturnable<Score> cir) {
        if (CachedScoreboardContents.isObjectiveRelevant(objective)) {
            CachedScoreboardContents.clearCache();
        }
    }

    @Inject(method = "resetPlayerScore", at = @At(value = "TAIL"))
    private void resetPlayerScore(String string, Objective objective, CallbackInfo ci) {
        if (CachedScoreboardContents.isObjectiveRelevant(objective)) {
            CachedScoreboardContents.clearCache();
        }
    }

    @Inject(method = "onScoreChanged", at = @At(value = "TAIL"))
    private void onScoreChanged(Score score, CallbackInfo ci) {
        if (CachedScoreboardContents.isObjectiveRelevant(score.getObjective())) {
            CachedScoreboardContents.clearCache();
        }
    }
}
