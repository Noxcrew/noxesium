package com.noxcrew.noxesium.core.fabric.mixin.feature.qib;

import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.core.fabric.feature.qib.QibBehaviorModule;
import com.noxcrew.noxesium.core.registry.CommonItemComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
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
        if (player == null || player.isSpectator()) return;
        var itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        var qibBehavior = itemStack.noxesium$getComponent(CommonItemComponentTypes.QIB_BEHAVIOR);
        if (qibBehavior == null || itemStack.isEmpty()) return;
        if (player.getCooldowns().isOnCooldown(itemStack)) return;
        NoxesiumApi.getInstance().getFeatureOptional(QibBehaviorModule.class).ifPresent((module) -> {
            if (module.attackItemBehavior(player, qibBehavior)) {
                player.onAttack();
                player.swing(InteractionHand.MAIN_HAND);
                cir.setReturnValue(true);
            }
        });
    }

    @Inject(
            method = "startUseItem",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"),
            cancellable = true)
    public void triggerUse(CallbackInfo ci, @Local InteractionHand hand) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || player.isSpectator()) return;
        var itemStack = player.getItemInHand(hand);
        var qibBehavior = itemStack.noxesium$getComponent(CommonItemComponentTypes.QIB_BEHAVIOR);
        if (qibBehavior == null || itemStack.isEmpty()) return;
        if (player.getCooldowns().isOnCooldown(itemStack)) return;
        NoxesiumApi.getInstance().getFeatureOptional(QibBehaviorModule.class).ifPresent((module) -> {
            if (module.useItemBehavior(player, qibBehavior)) {
                var useCooldown = itemStack.get(DataComponents.USE_COOLDOWN);
                if (useCooldown != null) {
                    useCooldown.apply(itemStack, player);
                }
                player.swing(hand);
                minecraft.gameRenderer.itemInHandRenderer.itemUsed(hand);
                ci.cancel();
            }
        });
    }
}
