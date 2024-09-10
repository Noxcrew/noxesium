package com.noxcrew.noxesium.mixin.legacy.culling;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Updates culling bounding boxes on entities after the resource pack reloads.
 *
 * Will be removed in a future version as using armor stands for models is
 * no longer recommended, use display entities instead which let you customise
 * the culling hitbox.
 */
@Mixin(LevelRenderer.class)
@Deprecated
public abstract class UpdateCullingOnReloadMixin {

    @Inject(method = "allChanged", at = @At("RETURN"))
    public void updateArmorStandCullingBounds(CallbackInfo ci) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        for (var entity : level.entitiesForRendering()) {
            if (entity instanceof ArmorStand armorStand) {
                armorStand.onSyncedDataUpdated(ArmorStand.DATA_HEAD_POSE);
            }
        }
    }
}
