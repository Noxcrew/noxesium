package com.noxcrew.noxesium.mixin.ui.render.ext;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerTabOverlay.class)
public interface PlayerTabOverlayExt {

    @Nullable
    @Accessor("header")
    Component getHeader();

    @Nullable
    @Accessor("footer")
    Component getFooter();
}
