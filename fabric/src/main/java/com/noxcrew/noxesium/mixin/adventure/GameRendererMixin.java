package com.noxcrew.noxesium.mixin.adventure;

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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides [shouldRenderBlockOutline] to use ServerRules for global CanPlaceOn and
 * CanDestroy tags.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private boolean renderBlockOutline;


    @Inject(method = "shouldRenderBlockOutline", at = @At("RETURN"), cancellable = true)
    public void shouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        // Ignore if the return value is true
        if (cir.getReturnValue()) return;

        // Ignore if we aren't rendering the outline
        if (!this.renderBlockOutline) return;

        // Ignore if we're not rendering the GUI
        Entity entity = this.minecraft.getCameraEntity();
        if (!(entity instanceof Player) || this.minecraft.options.hideGui) return;

        if (!((Player) entity).getAbilities().mayBuild) {
            HitResult hitResult = this.minecraft.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
                if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                    BlockInWorld blockInWorld = new BlockInWorld(this.minecraft.level, blockPos, false);
                    Registry<Block> registry = this.minecraft.level.registryAccess().registryOrThrow(Registries.BLOCK);

                    // Allow global can destroy or can place on to override and render the outline anyway
                    if (ServerRules.GLOBAL_CAN_DESTROY.get().test(registry, blockInWorld) || ServerRules.GLOBAL_CAN_PLACE_ON.get().test(registry, blockInWorld)) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }
}
