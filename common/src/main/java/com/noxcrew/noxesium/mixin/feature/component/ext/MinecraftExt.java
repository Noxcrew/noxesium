package com.noxcrew.noxesium.mixin.feature.component.ext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftExt {

    @Accessor("fontManager")
    FontManager getFontManager();
}
