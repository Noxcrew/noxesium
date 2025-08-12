package com.noxcrew.noxesium.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.fabric.NoxesiumMod;
import com.noxcrew.noxesium.fabric.feature.misc.TeamGlowHotkeys;
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
        return player != null
                && player.getAbilities().mayfly
                &&
                // Only allow players on teams
                entity.getTeam() != null
                &&
                // Check that the team color is in the glowing teams list
                NoxesiumMod.getInstance()
                        .getFeature(TeamGlowHotkeys.class)
                        .getGlowingTeams()
                        .contains(entity.getTeam().getColor());
    }
}
