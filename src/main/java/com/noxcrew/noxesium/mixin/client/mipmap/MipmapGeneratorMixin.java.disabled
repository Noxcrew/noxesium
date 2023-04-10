package com.noxcrew.noxesium.mixin.client.mipmap;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MipmapGenerator.class)
public abstract class MipmapGeneratorMixin {

    /**
     * Overrides the generate mip levels function to support any size of image.
     */
    @Inject(method = "generateMipLevels", at = @At("HEAD"), cancellable = true)
    private static void injected(NativeImage[] nativeImages, int targetLevels, CallbackInfoReturnable<NativeImage[]> cir) {
        if (targetLevels + 1 <= nativeImages.length) {
            cir.setReturnValue(nativeImages);
            return;
        }

        var newImages = new NativeImage[targetLevels + 1];
        newImages[0] = nativeImages[0];
        var transparency = hasTransparentPixel(newImages[0]);
        for (int level = 1; level <= targetLevels; ++level) {
            // Copy over any image that is already generated
            if (level < nativeImages.length) {
                newImages[level] = nativeImages[level];
                continue;
            }

            // Create a new image of the smaller size
            var sourceImage = newImages[level - 1];
            var newImage = new NativeImage(
                    Math.max(sourceImage.getWidth() >> 1, 1),
                    Math.max(sourceImage.getHeight() >> 1, 1),
                    false
            );
            var width = newImage.getWidth();
            var height = newImage.getHeight();
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    newImage.setPixelRGBA(
                            x,
                            y,
                            alphaBlend(
                                    getPixelRGBANullable(sourceImage, x * 2 + 0, y * 2 + 0),
                                    getPixelRGBANullable(sourceImage, x * 2 + 1, y * 2 + 0),
                                    getPixelRGBANullable(sourceImage, x * 2 + 0, y * 2 + 1),
                                    getPixelRGBANullable(sourceImage, x * 2 + 1, y * 2 + 1),
                                    transparency
                            )
                    );
                }
            }
            newImages[level] = newImage;
        }
        cir.setReturnValue(newImages);
    }

    /**
     * A custom variant of [getPixelRGBA] that returns null for out of bounds pixels.
     */
    private static Integer getPixelRGBANullable(NativeImage image, int x, int y) {
        if (image.format() != NativeImage.Format.RGBA) return null;
        if (image.isOutsideBounds(x, y)) return null;
        image.checkAllocated();
        var l = ((long) x + (long) y * (long) image.getWidth()) * 4L;
        return MemoryUtil.memGetInt(image.pixels + l);
    }

    /**
     * A custom variant of [alphaBlend] that supports up to 4 inputs.
     */
    private static int alphaBlend(Integer i, Integer j, Integer k, Integer l, boolean transparency) {
        // Determine how many pixels are being blended between
        var count = 0;
        if (i != null) count++;
        if (j != null) count++;
        if (k != null) count++;
        if (l != null) count++;

        if (transparency) {
            var f = 0.0f;
            var g = 0.0f;
            var h = 0.0f;
            var m = 0.0f;

            if (i != null && i >> 24 != 0) {
                f += getPow22(i >> 24);
                g += getPow22(i >> 16);
                h += getPow22(i >> 8);
                m += getPow22(i);
            }
            if (j != null && j >> 24 != 0) {
                f += getPow22(j >> 24);
                g += getPow22(j >> 16);
                h += getPow22(j >> 8);
                m += getPow22(j);
            }
            if (k != null && k >> 24 != 0) {
                f += getPow22(k >> 24);
                g += getPow22(k >> 16);
                h += getPow22(k >> 8);
                m += getPow22(k);
            }
            if (l != null && l >> 24 != 0) {
                f += getPow22(l >> 24);
                g += getPow22(l >> 16);
                h += getPow22(l >> 8);
                m += getPow22(l);
            }

            var countFloat = (float) count;
            var n = (int) (Math.pow(f / countFloat, 0.45454545454545453) * 255.0);
            var o = (int) (Math.pow(g / countFloat, 0.45454545454545453) * 255.0);
            var p = (int) (Math.pow(h / countFloat, 0.45454545454545453) * 255.0);
            var q = (int) (Math.pow(m / countFloat, 0.45454545454545453) * 255.0);
            if (n < 96) {
                n = 0;
            }
            return n << 24 | o << 16 | p << 8 | q;
        }

        var factor = 1.0f / count;
        var r = gammaBlend(i, j, k, l, 24, factor);
        var s = gammaBlend(i, j, k, l, 16, factor);
        var t = gammaBlend(i, j, k, l, 8, factor);
        var u = gammaBlend(i, j, k, l, 0, factor);
        return r << 24 | s << 16 | t << 8 | u;
    }

    private static int gammaBlend(Integer i, Integer j, Integer k, Integer l, int m, float factor) {
        var f = i == null ? 0f : getPow22(i >> m);
        var g = j == null ? 0f : getPow22(j >> m);
        var h = k == null ? 0f : getPow22(k >> m);
        var n = l == null ? 0f : getPow22(l >> m);
        var o = (float) ((double) ((float) Math.pow((double) (f + g + h + n) * factor, 0.45454545454545453)));
        return (int) ((double) o * 255.0);
    }

    @Shadow
    private static boolean hasTransparentPixel(NativeImage nativeImage) {
        throw new UnsupportedOperationException("Mixin error");
    }

    @Shadow
    private static float getPow22(int i) {
        throw new UnsupportedOperationException("Mixin error");
    }
}
