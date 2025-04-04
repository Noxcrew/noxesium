package com.noxcrew.noxesium.mixin.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hooks in to packet handling to override the [ClientboundEntityPositionSyncPacket].
 * Make entities controlled by vehicles run positionRider() instead of performing the
 * teleport since they'll get teleported anyway next tick. This effectively turns
 * this teleport packet into a forced teleport into loaded chunks if applicable.
 */
@Mixin(ClientPacketListener.class)
public abstract class FixVehicleTeleportMixin {

    @WrapOperation(
            method = "handleEntityPositionSync",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/Entity;hasIndirectPassenger(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean forceTeleportRider(Entity instance, Entity entity, Operation<Boolean> original) {
        if (instance.getVehicle() != null) {
            // Teleport the entity to be on top of their vehicle
            instance.getVehicle().positionRider(instance);
        }
        return original.call(instance, entity);
    }
}
