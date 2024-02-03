package com.noxcrew.noxesium.mixin.rules.adventure;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Overrides [shouldRenderBlockOutline] to use ServerRules for global CanPlaceOn and
 * CanDestroy tags.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    Minecraft minecraft;

    @Shadow
    private boolean renderBlockOutline;


    @ModifyReturnValue(method = "shouldRenderBlockOutline", at = @At("RETURN"))
    public boolean shouldRenderBlockOutline(boolean original) {
        // Ignore if the return value is true
        if (original) return true;

        // Ignore if we aren't rendering the outline
        if (!this.renderBlockOutline) return false;

        // Ignore if we're not rendering the GUI
        Entity entity = this.minecraft.getCameraEntity();
        if (!(entity instanceof Player player) || this.minecraft.options.hideGui) return false;
        if (player.getAbilities().mayBuild) return false;

        HitResult hitResult = this.minecraft.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return false;

        BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) return false;

        BlockInWorld blockInWorld = new BlockInWorld(this.minecraft.level, blockPos, false);
        Registry<Block> registry = this.minecraft.level.registryAccess().registryOrThrow(Registries.BLOCK);

        // Allow global can destroy or can place on to override and render the outline anyway
        return ServerRules.GLOBAL_CAN_DESTROY.getValue()
                .test(registry, blockInWorld) || ServerRules.GLOBAL_CAN_PLACE_ON.getValue()
                .test(registry, blockInWorld);
    }
}
