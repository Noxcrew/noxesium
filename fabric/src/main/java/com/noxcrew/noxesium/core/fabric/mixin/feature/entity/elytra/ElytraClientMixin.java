package com.noxcrew.noxesium.core.fabric.mixin.feature.entity.elytra;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.core.fabric.feature.entity.FallFlyingEntityExtension;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundGlidePacket;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.ElytraOnPlayerSoundInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces vanilla elytra handling with a custom client-side value that is used
 * instead of waiting for the server.
 */
@Mixin(LivingEntity.class)
public abstract class ElytraClientMixin implements FallFlyingEntityExtension {
    @Shadow
    protected abstract boolean canGlide();

    @Shadow
    public abstract void stopFallFlying();

    @Unique
    private boolean noxesium$fallFlying = false;

    @Unique
    private int noxesium$elytraCoyoteTime = 0;

    @Unique
    @Nullable
    private ElytraOnPlayerSoundInstance noxesium$elytraSoundInstance = null;

    @Override
    public void noxesium$startFallFlying() {
        noxesium$fallFlying = true;
        noxesium$elytraCoyoteTime = GameComponents.getInstance()
                .noxesium$getComponentOr(CommonGameComponentTypes.ELYTRA_COYOTE_TIME, () -> 0);
        NoxesiumServerboundNetworking.send(new ServerboundGlidePacket(true));

        // Play the sound while gliding for the local player!
        if (((Object) this) instanceof LocalPlayer localPlayer) {
            if (noxesium$elytraSoundInstance == null || noxesium$elytraSoundInstance.isStopped()) {
                noxesium$elytraSoundInstance = new ElytraOnPlayerSoundInstance(localPlayer);
                Minecraft.getInstance().getSoundManager().play(noxesium$elytraSoundInstance);
            }
        }
    }

    @Override
    public void noxesium$stopFallFlying() {
        noxesium$fallFlying = false;
        noxesium$elytraCoyoteTime = 0;
        NoxesiumServerboundNetworking.send(new ServerboundGlidePacket(false));
    }

    @Inject(method = "updateFallFlying", at = @At("RETURN"))
    public void updateFallFlying(CallbackInfo ci) {
        if (!GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_ELYTRA))
            return;
        if (((Object) this) != Minecraft.getInstance().player) return;

        if (canGlide()) {
            noxesium$elytraCoyoteTime = GameComponents.getInstance()
                    .noxesium$getComponentOr(CommonGameComponentTypes.ELYTRA_COYOTE_TIME, () -> 0);
        } else if (noxesium$elytraCoyoteTime > 0) {
            noxesium$elytraCoyoteTime--;
        } else {
            stopFallFlying();
        }
    }

    @Inject(method = "stopFallFlying", at = @At(value = "HEAD"), cancellable = true)
    private void stopFallFlying(CallbackInfo ci) {
        if (!GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_ELYTRA))
            return;
        if (((Object) this) != Minecraft.getInstance().player) return;
        ci.cancel();
        noxesium$stopFallFlying();
    }

    @Inject(method = "isFallFlying", at = @At(value = "HEAD"), cancellable = true)
    private void isFallFlying(CallbackInfoReturnable<Boolean> cir) {
        if (!GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_ELYTRA))
            return;
        if (((Object) this) != Minecraft.getInstance().player) return;
        cir.setReturnValue(noxesium$fallFlying);
    }
}
