package com.noxcrew.noxesium.mixin.render;

import com.noxcrew.noxesium.feature.render.cache.ScoreboardCache;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @Inject(method = "setPlayerPrefix", at = @At(value = "TAIL"))
    private void setPlayerPrefix(Component component, CallbackInfo ci) {
        if (ScoreboardCache.getInstance().isTeamRelevant(this.getName())) {
            ScoreboardCache.getInstance().clearCache();
        }
    }

    @Inject(method = "setPlayerSuffix", at = @At(value = "TAIL"))
    private void setPlayerSuffix(Component component, CallbackInfo ci) {
        if (ScoreboardCache.getInstance().isTeamRelevant(this.getName())) {
            ScoreboardCache.getInstance().clearCache();
        }
    }

    @Inject(method = "setColor", at = @At(value = "TAIL"))
    private void setColor(ChatFormatting chatFormatting, CallbackInfo ci) {
        if (ScoreboardCache.getInstance().isTeamRelevant(this.getName())) {
            ScoreboardCache.getInstance().clearCache();
        }
    }
}
