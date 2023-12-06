package com.noxcrew.noxesium.mixin.performance.render.ext;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Consumer;

@Mixin(OptionInstance.class)
public interface OptionInstanceExt {

    @Accessor("onValueUpdate")
    Consumer getOnValueUpdate();
}
