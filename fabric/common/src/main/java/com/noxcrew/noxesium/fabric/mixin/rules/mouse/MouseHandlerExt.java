package com.noxcrew.noxesium.fabric.mixin.rules.mouse;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandlerExt {

    @Accessor("accumulatedDX")
    void setAccumulatedDeltaX(double accumulatedDX);

    @Accessor("accumulatedDY")
    void setAccumulatedDeltaY(double accumulatedDY);
}
