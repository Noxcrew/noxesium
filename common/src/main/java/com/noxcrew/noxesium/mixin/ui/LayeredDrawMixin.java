package com.noxcrew.noxesium.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.feature.ui.layer.LayeredDrawExtension;
import com.noxcrew.noxesium.feature.ui.layer.NoxesiumLayer;
import com.noxcrew.noxesium.feature.ui.layer.NoxesiumLayeredDraw;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.BooleanSupplier;

/**
 * Hooks into LayeredDraw and delegates it to Noxesium's implementation.
 */
@Mixin(LayeredDraw.class)
public class LayeredDrawMixin implements LayeredDrawExtension {

    @Unique
    private final NoxesiumLayeredDraw noxesium$layeredDraw = new NoxesiumLayeredDraw();

    @Override
    public NoxesiumLayeredDraw noxesium$get() {
        return noxesium$layeredDraw;
    }

    @Override
    public void noxesium$addLayer(String name, LayeredDraw.Layer layer) {
        noxesium$layeredDraw.add(new NoxesiumLayer.Layer(name, layer));
    }

    @WrapMethod(method = "add(Lnet/minecraft/client/gui/LayeredDraw$Layer;)Lnet/minecraft/client/gui/LayeredDraw;")
    private LayeredDraw addLayer(LayeredDraw.Layer layer, Operation<LayeredDraw> original) {
        noxesium$layeredDraw.add(new NoxesiumLayer.Layer(layer));
        return ((LayeredDraw) (Object) this);
    }

    @WrapMethod(method = "add(Lnet/minecraft/client/gui/LayeredDraw;Ljava/util/function/BooleanSupplier;)Lnet/minecraft/client/gui/LayeredDraw;")
    private LayeredDraw addGroup(LayeredDraw layeredDraw, BooleanSupplier booleanSupplier, Operation<LayeredDraw> original) {
        noxesium$layeredDraw.add(new NoxesiumLayer.NestedLayers(((LayeredDrawExtension) layeredDraw).noxesium$get(), booleanSupplier));
        return ((LayeredDraw) (Object) this);
    }

    @WrapMethod(method = "render")
    private void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        noxesium$layeredDraw.render(guiGraphics, deltaTracker);
    }
}
