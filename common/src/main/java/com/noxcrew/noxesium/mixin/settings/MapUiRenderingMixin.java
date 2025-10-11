package com.noxcrew.noxesium.mixin.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noxcrew.noxesium.NoxesiumMod;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
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
            PoseStack p_109354_,
            SubmitNodeCollector p_440514_,
            int p_109356_,
            float p_109357_,
            HumanoidArm p_109358_,
            float p_109359_,
            ItemStack p_109360_,
            CallbackInfo ci) {
        if (NoxesiumMod.getInstance().getConfig().shouldRenderMapsInUi()) {
            ci.cancel();
        }
    }
}
