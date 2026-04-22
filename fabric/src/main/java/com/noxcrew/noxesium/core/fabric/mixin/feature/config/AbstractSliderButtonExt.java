package com.noxcrew.noxesium.core.fabric.mixin.feature.config;

import net.minecraft.client.gui.components.AbstractSliderButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractSliderButton.class)
public interface AbstractSliderButtonExt {
    @Invoker("setValue")
    void invokeSetValue(double newValue);
}
