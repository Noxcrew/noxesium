package com.noxcrew.noxesium.mixin.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noxcrew.noxesium.NoxesiumMod;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks in and removes rendering of the one-handed maps so we can render them another way.
 */
@Mixin(ItemInHandRenderer.class)
public class MapUiRenderingMixin {

    @Inject(method = "renderOneHandedMap", at = @At("HEAD"), cancellable = true)
    public void preventRenderingMap(
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int i,
            float f,
            HumanoidArm humanoidArm,
            float g,
            ItemStack itemStack,
            CallbackInfo ci) {
        if (NoxesiumMod.getInstance().getConfig().shouldRenderMapsInUi()) {
            ci.cancel();
        }
    }
}
