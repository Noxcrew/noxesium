package com.noxcrew.noxesium.mixin.feature;

import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for enforcing note block server updates.
 */
@Mixin(value = {NoteBlock.class})
public class ServerAuthoritativeNoteBlockUpdatesMixin {

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    public void onGetStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        if (!ServerRules.SERVER_AUTHORITATIVE_BLOCK_UPDATES.getValue()) return;
        cir.setReturnValue(((NoteBlock) (Object) this).defaultBlockState());
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    public void onUpdateShape(BlockState p_57645_, LevelReader p_374437_, ScheduledTickAccess p_374214_, BlockPos p_57649_, Direction p_57646_, BlockPos p_57650_, BlockState p_57647_, RandomSource p_374065_, CallbackInfoReturnable<BlockState> cir) {
        if (!ServerRules.SERVER_AUTHORITATIVE_BLOCK_UPDATES.getValue()) return;
        cir.setReturnValue(p_57645_);
    }

}
