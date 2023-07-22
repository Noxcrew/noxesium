package com.noxcrew.noxesium.mixin.info;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(OptionInstance.class)
public interface OptionInstanceExt {

    @Accessor("onValueUpdate")
    Consumer getOnValudUpdate();
}
