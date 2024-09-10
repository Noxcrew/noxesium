package com.noxcrew.noxesium.mixin.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hooks in to packet handling to override the [ClientboundTeleportEntityPacket].
 * Make entities controlled by vehicles run positionRider() instead of performing the
 * teleport since they'll get teleported anyway next tick. This effectively turns
 * this teleport packet into a forced teleport into loaded chunks if applicable.
 */
@Mixin(ClientPacketListener.class)
public abstract class FixVehicleTeleportMixin {

    @WrapOperation(method = "handleTeleportEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isControlledByLocalInstance()Z"))
    private boolean forceTeleportRider(Entity instance, Operation<Boolean> original) {
        if (instance.getVehicle() != null) {
            // Teleport the entity to be on top of their vehicle
            instance.getVehicle().positionRider(instance);

            // This makes the regular packet handler stop execution right here
            return true;
        }
        return original.call(instance);
    }
}
