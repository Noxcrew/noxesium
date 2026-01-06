package com.noxcrew.noxesium.core.fabric.mixin.feature.entity.elytra;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.core.fabric.feature.entity.FallFlyingEntityExtension;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Redirects when a player wants to start gliding to the custom logic.
 */
@Mixin(Player.class)
public abstract class PlayerStartGlidingHook {
    @Shadow
    protected abstract boolean canGlide();

    @Inject(method = "startFallFlying", at = @At("HEAD"), cancellable = true)
    public void onStartGlidingByForce(CallbackInfo ci) {
        if (!GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_ELYTRA))
            return;
        var player = Minecraft.getInstance().player;
        if (((Object) this) != player) return;
        ci.cancel();
        ((FallFlyingEntityExtension) this).noxesium$startFallFlying();
    }

    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    public void onStartGliding(CallbackInfoReturnable<Boolean> cir) {
        if (!GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_ELYTRA))
            return;
        var player = Minecraft.getInstance().player;
        if (((Object) this) != player) return;

        // Ignore if the player is already gliding, cannot glide, or is in water!
        if (player.isFallFlying() || !canGlide() || player.isInWater()) return;

        // Return false so the server is not sent a packet so we can do it custom
        // through Noxesium and don't end up with two sources of information.
        cir.setReturnValue(false);
        ((FallFlyingEntityExtension) this).noxesium$startFallFlying();
    }
}
