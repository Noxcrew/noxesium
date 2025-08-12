package com.noxcrew.noxesium.fabric.mixin.feature.component.ext;

import java.util.Map;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FontManager.class)
public interface FontManagerExt {

    @Accessor("fontSets")
    Map<ResourceLocation, FontSet> getFontSets();
}
