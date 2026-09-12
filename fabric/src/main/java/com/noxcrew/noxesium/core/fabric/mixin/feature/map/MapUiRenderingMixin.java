package com.noxcrew.noxesium.core.fabric.mixin.feature.map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks in and removes rendering of the one-handed maps so we can render them another way.
 */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class MapUiRenderingMixin {

    @Inject(method = "renderOneHandedMap", at = @At("HEAD"), cancellable = true)
    public void preventRenderingMap(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            float inverseArmHeight,
            HumanoidArm arm,
            float attackValue,
            ItemStack map,
            PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState state,
            CallbackInfo ci) {
        if (NoxesiumMod.getInstance().getConfig().shouldRenderMapsInUi()) {
            ci.cancel();
        }
    }
}
