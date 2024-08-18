package com.noxcrew.noxesium.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.feature.entity.SpatialInteractionEntityTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks in to packet handling to override the [ClientboundTeleportEntityPacket].
 * Make entities controlled by vehicles run positionRider() instead of performing the
 * teleport since they'll get teleported anyway next tick. This effectively turns
 * this teleport packet into a forced teleport into loaded chunks if applicable.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

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

    /**
     * Update client side packets whenever they are overridden by the server.
     */
    @Inject(method = "handleUpdateAttributes", at = @At("RETURN"))
    public void handleUpdateAttributes(ClientboundUpdateAttributesPacket packet, CallbackInfo ci) {
        if (Minecraft.getInstance().player == null) return;
        if (packet.getEntityId() == Minecraft.getInstance().player.getId()) {
            Minecraft.getInstance().player.noxesium$updateClientsidePotionEffects();
        }
    }

    /**
     * Clears the locally stored spatial entity tree on client level discarding.
     */
    @Inject(method = "clearLevel", at = @At("RETURN"))
    public void clearLevel(CallbackInfo ci) {
        SpatialInteractionEntityTree.clear();
    }
}
