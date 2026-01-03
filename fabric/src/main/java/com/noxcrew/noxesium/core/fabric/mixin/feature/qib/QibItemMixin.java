package com.noxcrew.noxesium.core.fabric.mixin.feature.qib;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.core.fabric.feature.qib.QibBehaviorModule;
import com.noxcrew.noxesium.core.registry.CommonItemComponentTypes;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds patches to items to let them trigger qib behaviors as their action.
 */
@Mixin(Minecraft.class)
public abstract class QibItemMixin {

    @Unique
    private static final Map<UUID, Long> NOXESIUM_LAST_USE_TICK = new HashMap<>();

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

        var main = player.getItemInHand(InteractionHand.MAIN_HAND);
        var mainBehavior = main.noxesium$getComponent(CommonItemComponentTypes.QIB_BEHAVIOR);
        if (mainBehavior != null) {
            if (player.getCooldowns().isOnCooldown(main)) return;
            var lastTick = NOXESIUM_LAST_USE_TICK.get(player.getUUID());
            var currentTick = player.level().getGameTime();
            if (lastTick != null && lastTick == currentTick) return;
            NoxesiumApi.getInstance()
                    .getFeatureOptional(QibBehaviorModule.class)
                    .ifPresent((module) -> {
                        module.useItemBehavior(player, mainBehavior);
                    });
            player.swing(InteractionHand.MAIN_HAND);
            NOXESIUM_LAST_USE_TICK.put(player.getUUID(), currentTick);
            return;
        }

        var off = player.getItemInHand(InteractionHand.OFF_HAND);
        var offBehavior = off.noxesium$getComponent(CommonItemComponentTypes.QIB_BEHAVIOR);
        if (offBehavior == null) return;
        if (player.getCooldowns().isOnCooldown(off)) return;
        var lastTick = NOXESIUM_LAST_USE_TICK.get(player.getUUID());
        var currentTick = player.level().getGameTime();
        if (lastTick != null && lastTick == currentTick) return;
        NoxesiumApi.getInstance().getFeatureOptional(QibBehaviorModule.class).ifPresent((module) -> {
            module.useItemBehavior(player, offBehavior);
        });
        player.swing(InteractionHand.OFF_HAND);
        NOXESIUM_LAST_USE_TICK.put(player.getUUID(), currentTick);
    }
}
