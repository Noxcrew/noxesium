package com.noxcrew.noxesium.core.fabric.mixin.feature.entity.elytra;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.core.fabric.feature.entity.FallFlyingEntityExtension;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundGlidePacket;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundLandPacket;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
    private Future<?> noxesium$elytraCoyoteTask = null;

    @Unique
    @Nullable
    private ElytraOnPlayerSoundInstance noxesium$elytraSoundInstance = null;

    @Override
    public void noxesium$startFallFlying() {
        noxesium$fallFlying = true;
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
        if (noxesium$elytraCoyoteTask != null) {
            noxesium$elytraCoyoteTask.cancel(true);
        }
        noxesium$elytraCoyoteTask = null;
        NoxesiumServerboundNetworking.send(new ServerboundGlidePacket(false));
    }

    /**
     * Check custom gliding at the start of push entities which is after travel ticking which means
     * the on ground state has been updated properly! This avoids a timer starting after we literally
     * just jumped.
     */
    @Inject(method = "pushEntities", at = @At("HEAD"))
    public void updateFallFlying(CallbackInfo ci) {
        if (!GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_ELYTRA))
            return;
        if (((Object) this) != Minecraft.getInstance().player) return;

        // Ignore if not fall flying
        if (!noxesium$fallFlying) return;

        // If you cannot glide anymore we cancel the gliding, possibly with a custom delay.
        // Gliding will always be interrupted, you cannot never stop gliding.
        if (!canGlide()) {
            if (noxesium$elytraCoyoteTask == null) {
                // Determine when the coyote time will end!
                var extraTime = GameComponents.getInstance()
                        .noxesium$getComponentOr(CommonGameComponentTypes.ELYTRA_COYOTE_TIME_MS, () -> 0L);
                if (extraTime <= 0) {
                    stopFallFlying();
                } else {
                    // Ask the server for permission to do a landing and
                    // temporary rebound if applicable. We schedule a stop
                    // flying command to come on in and get processed on
                    // the packet thread as if the server sends it after the
                    // exact coyote time.
                    NoxesiumServerboundNetworking.send(new ServerboundLandPacket());
                    noxesium$elytraCoyoteTask = NoxesiumApi.getInstance()
                            .getThreadPool()
                            .schedule(
                                    () -> {
                                        Minecraft.getInstance().schedule(this::stopFallFlying);
                                    },
                                    extraTime,
                                    TimeUnit.MILLISECONDS);
                }
            }
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
