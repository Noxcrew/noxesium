package com.noxcrew.noxesium.mixin.performance.render;

import com.noxcrew.noxesium.feature.render.cache.scoreboard.ScoreboardCache;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Listens to changes to the teams relevant to the scoreboard cache.
 */
@Mixin(PlayerTeam.class)
public abstract class PlayerTeamMixin {

    @Shadow
    public abstract String getName();

    @Inject(method = "setPlayerPrefix", at = @At("TAIL"))
    private void refreshScoreboardCacheOnSetPlayerPrefix(CallbackInfo ci) {
        noxesium$refreshScoreboard();
    }

    @Inject(method = "setPlayerSuffix", at = @At("TAIL"))
    private void refreshScoreboardCacheOnSetPlayerSuffix(CallbackInfo ci) {
        noxesium$refreshScoreboard();
    }

    @Inject(method = "setColor", at = @At("TAIL"))
    private void refreshScoreboardCacheOnSetColor(CallbackInfo ci) {
        noxesium$refreshScoreboard();
    }

    @Unique
    private void noxesium$refreshScoreboard() {
        if (ScoreboardCache.getInstance().isTeamRelevant(this.getName())) {
            ScoreboardCache.getInstance().clearCache();
        }
    }
}
