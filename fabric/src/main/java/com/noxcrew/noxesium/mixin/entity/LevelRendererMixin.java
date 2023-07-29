package com.noxcrew.noxesium.mixin.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Updates culling bounding boxes on entities after the resource pack reloads.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method = "allChanged", at = @At("RETURN"))
    public void allChanged(CallbackInfo ci) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        for (var entity : level.entitiesForRendering()) {
            if (entity instanceof ArmorStand armorStand) {
                armorStand.onSyncedDataUpdated(ArmorStand.DATA_HEAD_POSE);
            }
        }
    }
}
