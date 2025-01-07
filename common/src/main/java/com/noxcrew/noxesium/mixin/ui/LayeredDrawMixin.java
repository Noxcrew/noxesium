package com.noxcrew.noxesium.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.ui.layer.LayeredDrawExtension;
import com.noxcrew.noxesium.feature.ui.layer.NoxesiumLayer;
import com.noxcrew.noxesium.feature.ui.layer.NoxesiumLayeredDraw;
import com.noxcrew.noxesium.feature.ui.render.SharedVertexBuffer;
import java.util.function.BooleanSupplier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Hooks into LayeredDraw and delegates it to Noxesium's implementation.
 */
@Mixin(LayeredDraw.class)
public class LayeredDrawMixin implements LayeredDrawExtension {

    @Unique
    private final NoxesiumLayeredDraw noxesium$layeredDraw = new NoxesiumLayeredDraw();

    @Unique
    private boolean noxesium$ignoreAdditions = false;

    @Override
    public NoxesiumLayeredDraw noxesium$get() {
        return noxesium$layeredDraw;
    }

    @Override
    public void noxesium$addLayer(String name, LayeredDraw.Layer layer) {
        noxesium$layeredDraw.add(new NoxesiumLayer.Layer(name, layer));
        noxesium$ignoreAdditions = true;
        ((LayeredDraw) (Object) this).add(layer);
        noxesium$ignoreAdditions = false;
    }

    @WrapMethod(method = "add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    private LayeredDraw addLayer(LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        if (!noxesium$ignoreAdditions) noxesium$layeredDraw.add(new NoxesiumLayer.Layer(layer));
        return original.call(layer);
    }

    @WrapMethod(
            method =
                    "add(Lnet/minecraft/client/gui/LayeredDraw;Ljava/util/function/BooleanSupplier;)Lnet/minecraft/client/gui/LayeredDraw;")
    private LayeredDraw addGroup(
            LayeredDraw layeredDraw, BooleanSupplier booleanSupplier, Operation<LayeredDraw> original) {
        noxesium$layeredDraw.add(
                new NoxesiumLayer.NestedLayers(((LayeredDrawExtension) layeredDraw).noxesium$get(), booleanSupplier));
        return original.call(layeredDraw, booleanSupplier);
    }

    @WrapMethod(method = "render")
    private void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        // Try to render through the UI limiting system, otherwise fall back to vanilla!
        if (NoxesiumMod.getInstance().getConfig().shouldUseDynamicUiLimiting()) {
            if (noxesium$layeredDraw.render(guiGraphics, deltaTracker)) return;

            // Reset the state if the rendering fails!
            SharedVertexBuffer.reset();
        } else {
            noxesium$layeredDraw.reset();
        }
        original.call(guiGraphics, deltaTracker);
    }
}
