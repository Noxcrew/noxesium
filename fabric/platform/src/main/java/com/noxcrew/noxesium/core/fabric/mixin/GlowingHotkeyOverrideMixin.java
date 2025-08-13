package com.noxcrew.noxesium.core.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.core.fabric.feature.misc.TeamGlowHotkeys;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Overrides whether entities should appear as glowing.
 */
@Mixin(Minecraft.class)
public class GlowingHotkeyOverrideMixin {

    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean checkIfToggledTeamGlowing(boolean original, Entity entity) {
        if (original) return true;
        var player = Minecraft.getInstance().player;

        // Only allow using the glowing outlines when flying is allowed == they are spectating
        if (player == null || !player.getAbilities().mayfly || entity.getTeam() == null) return false;

        // Check for the custom hotkeys
        var teamGlowHotkeys = NoxesiumApi.getInstance().getFeatureOrNull(TeamGlowHotkeys.class);
        if (teamGlowHotkeys == null) return false;

        // Check that the team color is in the glowing teams list
        return teamGlowHotkeys.getGlowingTeams().contains(entity.getTeam().getColor());
    }
}
