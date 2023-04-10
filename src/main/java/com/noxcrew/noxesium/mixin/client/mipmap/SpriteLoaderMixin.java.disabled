package com.noxcrew.noxesium.mixin.client.mipmap;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.StitcherException;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Mixin(SpriteLoader.class)
public abstract class SpriteLoaderMixin {

    @Shadow @Final private int maxSupportedTextureSize;
    @Shadow @Final private int minWidth;
    @Shadow @Final private int minHeight;

    @Shadow protected abstract Map<ResourceLocation, TextureAtlasSprite> getStitchedSprites(Stitcher<SpriteContents> stitcher, int i, int j);

    /**
     * Overrides the stitch function to allow any sized image to be stitched into the mipmaps.
     * This is simply a copy of vanilla logic with the logic to limit mipmap levels removed so
     * we always create the maximum mipmap levels configured.
     */
    @Inject(method = "stitch", at = @At("HEAD"), cancellable = true)
    private void injected(List<SpriteContents> list, int maxMipmapLevels, Executor executor, CallbackInfoReturnable<SpriteLoader.Preparations> cir) {
        var maxSize = maxSupportedTextureSize;
        var stitcher = new Stitcher<SpriteContents>(maxSize, maxSize, maxMipmapLevels);
        for (SpriteContents spriteContents : list) {
            stitcher.registerSprite(spriteContents);
        }

        try {
            stitcher.stitch();
        } catch (StitcherException stitcherException) {
            CrashReport crashReport = CrashReport.forThrowable(stitcherException, "Stitching");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Stitcher");
            crashReportCategory.setDetail("Sprites", stitcherException.getAllSprites().stream().map(entry -> String.format(Locale.ROOT, "%s[%dx%d]", entry.name(), entry.width(), entry.height())).collect(Collectors.joining(",")));
            crashReportCategory.setDetail("Max Texture Size", maxSize);
            throw new ReportedException(crashReport);
        }

        var width = Math.max(stitcher.getWidth(), minWidth);
        var height = Math.max(stitcher.getHeight(), minHeight);
        var map = getStitchedSprites(stitcher, width, height);
        var textureAtlasSprite = map.get(MissingTextureAtlasSprite.getLocation());
        var completableFuture = CompletableFuture.runAsync(() -> map.values().forEach(tas -> tas.contents().increaseMipLevel(maxMipmapLevels)), executor);
        cir.setReturnValue(new SpriteLoader.Preparations(width, height, maxMipmapLevels, textureAtlasSprite, map, completableFuture));
    }
}
