package com.noxcrew.noxesium.mixin.entity;

import com.noxcrew.noxesium.feature.entity.ExtraEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes interactions with qib behaviors not function as interaction entities.
 * It's assumed the server also ignores interactions with these entities.
 */
@Mixin(Interaction.class)
public class InteractionMixin {

    @Inject(method = "skipAttackInteraction", at = @At("HEAD"), cancellable = true)
    public void skipAttackInteraction(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        var interaction = (Interaction) ((Object) this);
        if (interaction.hasExtraData(ExtraEntityData.QIB_BEHAVIOR)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    public void interact(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        var interaction = (Interaction) ((Object) this);
        if (interaction.hasExtraData(ExtraEntityData.QIB_BEHAVIOR)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
