package com.noxcrew.noxesium.mixin.ui.ext;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.LayeredDraw;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Allows accessing the layers of the gui.
 */
@Mixin(Gui.class)
public interface GuiExt {

    @Accessor("layers")
    LayeredDraw getLayers();
}
