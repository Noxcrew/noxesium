package com.noxcrew.noxesium.core.fabric.mixin.rules.qib;

import com.noxcrew.noxesium.core.fabric.feature.entity.HitboxHelper;
import com.noxcrew.noxesium.core.fabric.registry.CommonEntityComponentTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes interactions with qib behaviors not function as interaction entities.
 * It's assumed the server also ignores interactions with these entities.
 */
@Mixin(Interaction.class)
public abstract class QibInteractionMixin {

    @Inject(method = "skipAttackInteraction", at = @At("HEAD"), cancellable = true)
    public void skipAttackInteraction(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        var interaction = (Interaction) ((Object) this);
        if (interaction.noxesium$hasComponent(CommonEntityComponentTypes.QIB_BEHAVIOR)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    public void interact(
            Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        var interaction = (Interaction) ((Object) this);
        if (interaction.noxesium$hasComponent(CommonEntityComponentTypes.QIB_BEHAVIOR)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "makeBoundingBox", at = @At("HEAD"), cancellable = true)
    public void makeBoundingBox(Vec3 position, CallbackInfoReturnable<AABB> cir) {
        var override = HitboxHelper.getBoundingBox((Interaction) ((Object) this), position);
        if (override != null) {
            cir.setReturnValue(override);
        }
    }
}
