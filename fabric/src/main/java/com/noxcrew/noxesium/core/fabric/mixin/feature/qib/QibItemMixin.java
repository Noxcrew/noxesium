package com.noxcrew.noxesium.core.fabric.mixin.feature.qib;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.core.fabric.feature.qib.QibBehaviorModule;
import com.noxcrew.noxesium.core.registry.CommonItemComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.UseCooldown;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds patches to items to let them trigger qib behaviors as their action.
 */
@Mixin(Minecraft.class)
public abstract class QibItemMixin {

    @Inject(
            method = "startAttack",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;",
                            ordinal = 0,
                            shift = At.Shift.BEFORE),
            cancellable = true)
    public void triggerAttack(CallbackInfoReturnable<Boolean> cir) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        var itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        var qibBehavior = itemStack.noxesium$getComponent(CommonItemComponentTypes.QIB_BEHAVIOR);
        if (qibBehavior == null || player.isSpectator()) return;
        NoxesiumApi.getInstance().getFeatureOptional(QibBehaviorModule.class).ifPresent((module) -> {
            module.attackItemBehavior(player, qibBehavior);
        });
        player.onAttack();
        player.swing(InteractionHand.MAIN_HAND);
        cir.setReturnValue(true);
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    public void triggerUse(CallbackInfo ci) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || player.isSpectator()) return;
        var itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        var qibBehavior = itemStack.noxesium$getComponent(CommonItemComponentTypes.QIB_BEHAVIOR);
        if (qibBehavior == null || itemStack.isEmpty()) return;
        if (player.getCooldowns().isOnCooldown(itemStack)) return;
        NoxesiumApi.getInstance().getFeatureOptional(QibBehaviorModule.class).ifPresent((module) -> {
            module.useItemBehavior(player, qibBehavior);
        });
        UseCooldown useCooldown = itemStack.get(DataComponents.USE_COOLDOWN);
        if (useCooldown != null) {
            int cooldownTicks = Math.max(0, Math.round(useCooldown.seconds() * 20.0f));
            if (cooldownTicks > 0) {
                player.getCooldowns().addCooldown(itemStack, cooldownTicks);
            }
        }
    }
}
