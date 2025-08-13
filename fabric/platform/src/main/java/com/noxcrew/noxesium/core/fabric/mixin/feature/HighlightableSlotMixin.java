package com.noxcrew.noxesium.core.fabric.mixin.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.noxcrew.noxesium.core.fabric.registry.CommonItemComponentTypes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adds customisation to highlightable slots.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class HighlightableSlotMixin {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @WrapOperation(
            method = "renderSlotHighlightBack",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    public void updateBackHighlight(
            GuiGraphics instance,
            RenderPipeline renderPipeline,
            ResourceLocation resourceLocation,
            int i,
            int j,
            int k,
            int l,
            Operation<Void> original) {
        var slot = hoveredSlot;
        if (slot != null && slot.getItem() != null) {
            var highlightable = slot.getItem().noxesium$getComponent(CommonItemComponentTypes.HOVERABLE);
            if (highlightable != null) {
                // Render a customisable sprite if the slot is marked as hoverable
                if (highlightable.hoverable())
                    original.call(
                            instance, renderPipeline, highlightable.backSprite().orElse(resourceLocation), i, j, k, l);
                return;
            }
        }
        original.call(instance, renderPipeline, resourceLocation, i, j, k, l);
    }

    @WrapOperation(
            method = "renderSlotHighlightFront",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    public void updateFrontHighlight(
            GuiGraphics instance,
            RenderPipeline renderPipeline,
            ResourceLocation resourceLocation,
            int i,
            int j,
            int k,
            int l,
            Operation<Void> original) {
        var slot = hoveredSlot;
        if (slot != null && slot.getItem() != null) {
            var highlightable = slot.getItem().noxesium$getComponent(CommonItemComponentTypes.HOVERABLE);
            if (highlightable != null) {
                // Render a customisable sprite if the slot is marked as hoverable
                if (highlightable.hoverable())
                    original.call(
                            instance,
                            renderPipeline,
                            highlightable.frontSprite().orElse(resourceLocation),
                            i,
                            j,
                            k,
                            l);
                return;
            }
        }
        original.call(instance, renderPipeline, resourceLocation, i, j, k, l);
    }
}
