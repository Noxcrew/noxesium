package com.noxcrew.noxesium.mixin.rules.adventure;

import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides [blockActionRestricted] to hook into a ServerRule which allows a server to
 * reference a tag to use as CanDestroy regardless of item used.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Shadow
    public abstract boolean mayBuild();

    @Inject(method = "blockActionRestricted", at = @At("RETURN"), cancellable = true)
    private void injected(Level level, BlockPos blockPos, GameType gameType, CallbackInfoReturnable<Boolean> cir) {
        // Don't change the return value in general cases
        if (!gameType.isBlockPlacingRestricted() || gameType == GameType.SPECTATOR || mayBuild()) {
            return;
        }

        // Only override it if you're being denied the block modification
        if (cir.getReturnValue()) {
            cir.setReturnValue(!ServerRules.GLOBAL_CAN_DESTROY.getValue().test(level.registryAccess().registryOrThrow(Registries.BLOCK), new BlockInWorld(level, blockPos, false)));
        }
    }

    @Redirect(method = "mayUseItemAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hasAdventureModePlaceTagForBlock(Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/block/state/pattern/BlockInWorld;)Z"))
    private boolean injected(ItemStack instance, Registry<Block> registry, BlockInWorld blockInWorld) {
        if (instance.hasAdventureModePlaceTagForBlock(registry, blockInWorld)) {
            return true;
        }

        // Also allow usage when the global can place on allows it
        return ServerRules.GLOBAL_CAN_PLACE_ON.getValue().test(registry, blockInWorld);
    }
}
