package com.noxcrew.noxesium.core.fabric.mixin.feature.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.noxcrew.noxesium.core.registry.CommonItemComponentTypes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
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
            method = "extractSlotHighlightBack",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    public void updateBackHighlight(
            GuiGraphicsExtractor instance,
            RenderPipeline renderPipeline,
            Identifier location,
            int x,
            int y,
            int width,
            int height,
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
                            highlightable
                                    .backSprite()
                                    .map(it -> Identifier.parse(it.asString()))
                                    .orElse(location),
                            x,
                            y,
                            width,
                            height);
                return;
            }
        }
        original.call(instance, renderPipeline, location, x, y, width, height);
    }

    @WrapOperation(
            method = "extractSlotHighlightFront",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    public void updateFrontHighlight(
            GuiGraphicsExtractor instance,
            RenderPipeline renderPipeline,
            Identifier location,
            int x,
            int y,
            int width,
            int height,
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
                            highlightable
                                    .frontSprite()
                                    .map(it -> Identifier.parse(it.asString()))
                                    .orElse(location),
                            x,
                            y,
                            width,
                            height);
                return;
            }
        }
        original.call(instance, renderPipeline, location, x, y, width, height);
    }
}
