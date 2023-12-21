package com.noxcrew.noxesium.mixin.performance.render;

import com.noxcrew.noxesium.feature.render.cache.scoreboard.ScoreboardCache;
import com.noxcrew.noxesium.feature.render.cache.tablist.TabListCache;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScores;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Listens to changes to the scoreboard relevant to the caches.
 */
@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin {

    @Shadow
    @Final
    private Map<String, PlayerScores> playerScores;

    @Inject(method = "addPlayerToTeam", at = @At("TAIL"))
    private void refreshScoreboardOnAddPlayerToTeam(String string, PlayerTeam playerTeam, CallbackInfoReturnable<Boolean> cir) {
        if (ScoreboardCache.getInstance().isPlayerRelevant(string)) {
            ScoreboardCache.getInstance().clearCache();
        }
    }

    @Inject(method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V", at = @At("TAIL"))
    private void refreshScoreboardOnRemovePlayerFromTeam(String string, PlayerTeam playerTeam, CallbackInfo ci) {
        if (ScoreboardCache.getInstance().isPlayerRelevant(string)) {
            ScoreboardCache.getInstance().clearCache();
        }
    }

    @Inject(method = "removePlayerTeam", at = @At("TAIL"))
    private void refreshScoreboardOnRemovePlayerTeam(PlayerTeam playerTeam, CallbackInfo ci) {
        if (ScoreboardCache.getInstance().isTeamRelevant(playerTeam.getName())) {
            ScoreboardCache.getInstance().clearCache();
        }
    }

    @Inject(method = "setDisplayObjective", at = @At("TAIL"))
    private void refreshScoreboardAndTabListOnSetDisplayObjective(DisplaySlot displaySlot, Objective objective, CallbackInfo ci) {
        // Inform the tab list whenever the tab list slot changes.
        if (displaySlot == DisplaySlot.LIST) {
            TabListCache.getInstance().clearCache();
        }

        // We don't need to care about changes to the below name or list slots.
        if (displaySlot == DisplaySlot.BELOW_NAME || displaySlot == DisplaySlot.LIST) return;

        // We do listen to any change to a team slot as the player could be seeing one
        // of those and notice it get overridden.
        ScoreboardCache.getInstance().clearCache();
    }

    @Inject(method = "getOrCreatePlayerScore(Lnet/minecraft/world/scores/ScoreHolder;Lnet/minecraft/world/scores/Objective;Z)Lnet/minecraft/world/scores/ScoreAccess;", at = @At("TAIL"))
    private void refreshScoreboardAndTabListOnGetOrCreatePlayerScore(ScoreHolder scoreHolder, Objective objective, boolean bl, CallbackInfoReturnable<ScoreAccess> cir) {
        noxesium$refreshScoreboardAndTabListForObjective(objective);
    }

    @Inject(method = "resetAllPlayerScores", at = @At("HEAD"))
    private void refreshScoreboardAndTabListOnResetAllPlayerScores(ScoreHolder scoreHolder, CallbackInfo ci) {
        var playerScores = this.playerScores.get(scoreHolder.getScoreboardName());
        if (playerScores == null) return;

        if (playerScores.listScores().keySet().stream().anyMatch(f -> ScoreboardCache.getInstance().isObjectiveRelevant(f))) {
            ScoreboardCache.getInstance().clearCache();
        }
        if (playerScores.listScores().keySet().stream().anyMatch(f -> TabListCache.getInstance().isObjectiveRelevant(f))) {
            TabListCache.getInstance().clearCache();
        }
    }

    @Inject(method = "resetSinglePlayerScore", at = @At("TAIL"))
    private void refreshScoreboardAndTabListOnResetSinglePlayerScore(ScoreHolder scoreHolder, Objective objective, CallbackInfo ci) {
        noxesium$refreshScoreboardAndTabListForObjective(objective);
    }

    @Inject(method = "onScoreChanged", at = @At("TAIL"))
    private void refreshScoreboardAndTabListOnScoreChanged(ScoreHolder scoreHolder, Objective objective, Score score, CallbackInfo ci) {
        noxesium$refreshScoreboardAndTabListForObjective(objective);
    }

    @Inject(method = "onScoreLockChanged", at = @At("TAIL"))
    private void refreshScoreboardAndTabListOnScoreLock(ScoreHolder scoreHolder, Objective objective, CallbackInfo ci) {
        noxesium$refreshScoreboardAndTabListForObjective(objective);
    }

    @Inject(method = "onObjectiveChanged", at = @At("TAIL"))
    private void refreshScoreboardAndTabListOnObjectiveChanged(Objective objective, CallbackInfo ci) {
        noxesium$refreshScoreboardAndTabListForObjective(objective);
    }

    @Unique
    private void noxesium$refreshScoreboardAndTabListForObjective(Objective objective) {
        if (ScoreboardCache.getInstance().isObjectiveRelevant(objective)) {
            ScoreboardCache.getInstance().clearCache();
        }
        if (TabListCache.getInstance().isObjectiveRelevant(objective)) {
            TabListCache.getInstance().clearCache();
        }
    }
}
