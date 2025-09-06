package com.noxcrew.noxesium.core.fabric.mixin.feature.component.ext;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SkinTextureDownloader.class)
public interface SkinTextureDownloaderExt {

    @Invoker("registerTextureInManager")
    static CompletableFuture<ResourceLocation> invokeRegisterTextureInManager(
            ResourceLocation resourceLocation, NativeImage nativeImage) {
        throw new UnsupportedOperationException("Invalid operation");
    }

    @Invoker("processLegacySkin")
    static NativeImage invokeProcessLegacySkin(NativeImage nativeImage, String string) {
        throw new UnsupportedOperationException("Invalid operation");
    }

    @Invoker("downloadSkin")
    static NativeImage invokeDownloadSkin(Path path, String string) throws IOException {
        throw new UnsupportedOperationException("Invalid operation");
    }
}
