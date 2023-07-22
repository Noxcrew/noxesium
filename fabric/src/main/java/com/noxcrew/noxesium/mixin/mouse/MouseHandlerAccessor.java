package com.noxcrew.noxesium.mixin.mouse;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {

    @Accessor("accumulatedDX")
    public void setAccumulatedDeltaX(double accumulatedDX);

    @Accessor("accumulatedDY")
    public void setAccumulatedDeltaY(double accumulatedDY);
}
