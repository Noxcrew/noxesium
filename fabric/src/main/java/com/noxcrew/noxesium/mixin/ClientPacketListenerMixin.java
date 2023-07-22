package com.noxcrew.noxesium.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Hooks in to packet handling to override the [ClientboundTeleportEntityPacket].
 * Make entities controlled by vehicles run positionRider() instead of performing the
 * teleport since they'll get teleported anyway next tick. This effectively turns
 * this teleport packet into a forced teleport into loaded chunks if applicable.
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Redirect(method = "handleTeleportEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isControlledByLocalInstance()Z"))
    private boolean injected(Entity entity) {
        if (entity.getVehicle() != null) {
            // Teleport the entity to be on top of their vehicle
            entity.getVehicle().positionRider(entity);

            // This makes the regular packet handler stop execution right here
            return true;
        }
        return entity.isControlledByLocalInstance();
    }
}
