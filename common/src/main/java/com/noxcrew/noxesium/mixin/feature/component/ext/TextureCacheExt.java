package com.noxcrew.noxesium.mixin.feature.component.ext;

import java.nio.file.Path;
import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SkinManager.TextureCache.class)
public interface TextureCacheExt {

    @Accessor("root")
    Path getRootPath();
}
