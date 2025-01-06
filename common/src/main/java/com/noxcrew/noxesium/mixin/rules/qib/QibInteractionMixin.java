package com.noxcrew.noxesium.mixin.rules.qib;

import com.noxcrew.noxesium.feature.entity.ExtraEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes interactions with qib behaviors not function as interaction entities.
 * It's assumed the server also ignores interactions with these entities.
 */
@Mixin(Interaction.class)
public class QibInteractionMixin {

    @Inject(method = "skipAttackInteraction", at = @At("HEAD"), cancellable = true)
    public void skipAttackInteraction(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        var interaction = (Interaction) ((Object) this);
        if (interaction.noxesium$hasExtraData(ExtraEntityData.QIB_BEHAVIOR)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    public void interact(
            Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        var interaction = (Interaction) ((Object) this);
        if (interaction.noxesium$hasExtraData(ExtraEntityData.QIB_BEHAVIOR)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "makeBoundingBox", at = @At("HEAD"), cancellable = true)
    public void interact(CallbackInfoReturnable<AABB> cir) {
        var interaction = (Interaction) ((Object) this);
        if (interaction.noxesium$hasExtraData(ExtraEntityData.QIB_WIDTH_Z)) {
            var dimensions = interaction.getDimensions(Pose.STANDING);
            var position = interaction.position();
            var x = position.x;
            var y = position.y;
            var z = position.z;
            var dx = dimensions.width() / 2.0f;
            var dy = dimensions.height();
            var dz = interaction.noxesium$getExtraData(ExtraEntityData.QIB_WIDTH_Z) / 2.0f;
            cir.setReturnValue(
                    new AABB(x - (double) dx, y, z - (double) dz, x + (double) dx, y + (double) dy, z + (double) dz));
        }
    }
}
