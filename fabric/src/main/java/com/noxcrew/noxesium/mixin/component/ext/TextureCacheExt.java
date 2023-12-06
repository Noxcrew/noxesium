package com.noxcrew.noxesium.mixin.component.ext;

import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(SkinManager.TextureCache.class)
public interface TextureCacheExt {

    @Accessor("root")
    Path getRootPath();

}
