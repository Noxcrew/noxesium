package com.noxcrew.noxesium.core.fabric.mixin.rules.qib;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.core.fabric.feature.entity.QibBehaviorModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds various hooks for the custom qib system.
 */
@Mixin(ClientPacketListener.class)
public abstract class QibPacketHooksMixin {

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
        noxesium$resetEntities();
    }

    /**
     * Clears the locally stored spatial entity tree when the level tree
     * is swapped out.
     */
    @Inject(method = "handleLogin", at = @At("RETURN"))
    public void handleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        noxesium$resetEntities();
    }

    /**
     * Clears the locally stored spatial entity tree on respawning in a different dimension.
     */
    @Inject(method = "handleRespawn", at = @At("HEAD"))
    public void handleRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        var commonplayerspawninfo = packet.commonPlayerSpawnInfo();
        var resourcekey = commonplayerspawninfo.dimension();
        var localplayer = Minecraft.getInstance().player;
        if (localplayer == null) return;
        var resourcekey1 = localplayer.level().dimension();
        var flag = resourcekey != resourcekey1;
        if (flag) noxesium$resetEntities();
    }

    /**
     * Resets all entities in the spatial interaction entity tree.
     */
    @Unique
    private void noxesium$resetEntities() {
        NoxesiumApi.getInstance().getFeatureOptional(QibBehaviorModule.class).ifPresent(it -> it.getSpatialTree()
                .clear());

        if (Minecraft.getInstance().player == null) return;
        Minecraft.getInstance().player.noxesium$clearClientsidePotionEffects();
    }
}
