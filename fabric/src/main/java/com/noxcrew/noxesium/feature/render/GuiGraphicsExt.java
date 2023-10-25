package com.noxcrew.noxesium.feature.render;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Provides additional methods for GuiGraphics.
 */
public class GuiGraphicsExt {

    private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);

    /**
     * Draws a new string into the given graphics based on the given baked component.
     * It is assumed we are running in managed mode.
     */
    public static int drawString(GuiGraphics graphics, Font font, BakedComponent bakedComponent, int x, int y, int color, boolean shadow) {
        var matrix = graphics.pose.last().pose();
        var bufferSource = graphics.bufferSource;
        var displayMode = Font.DisplayMode.NORMAL;
        var background = 0;
        var packedLightCoords = 15728880;

        color = Font.adjustColor(color);
        var matrixCopy = matrix;
        if (shadow) {
            matrixCopy = new Matrix4f(matrix);
            bakedComponent.renderOutput.finish(font, x, y, color, true, matrix, bufferSource, displayMode, background, packedLightCoords);
            matrixCopy.translate(SHADOW_OFFSET);
        }

        var fx = bakedComponent.renderOutput.finish(font, x, y, color, false, matrixCopy, bufferSource, displayMode, background, packedLightCoords);
        return (int) fx + (shadow ? 1 : 0);
    }

    /**
     * A formatted character sink that takes in characters separately from performing the final rendering.
     */
    public static class StringRenderOutput implements FormattedCharSink {

        private static final RandomSource RANDOM = RandomSource.create();
        private final List<FontCharacter> characters = new ArrayList<>();
        private final Font font;
        private final BakedGlyph baseBakedGlyph;

        private float x;
        private boolean containsObfuscation = false;

        public StringRenderOutput(Font font) {
            this.font = font;
            this.baseBakedGlyph = font.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
        }

        /**
         * Whether this output contains any obfuscated text.
         */
        public boolean doesContainObfuscation() {
            return containsObfuscation;
        }

        /**
         * Accepts a new character in the given position and style.
         */
        public boolean accept(int position, Style style, int codePoint) {
            var fontset = font.getFontSet(style.getFont());
            var glyphinfo = fontset.getGlyphInfo(codePoint, font.filterFishyGlyphs);

            // Determine information about the character
            var bold = style.isBold();
            var textcolor = style.getColor();
            Float r = null;
            Float g = null;
            Float b = null;
            if (textcolor != null) {
                var color = textcolor.getValue();
                r = (float) (color >> 16 & 255) / 255.0F;
                g = (float) (color >> 8 & 255) / 255.0F;
                b = (float) (color & 255) / 255.0F;
            }

            // Store the baked character
            var advance = glyphinfo.getAdvance(bold);
            var bakedGlyph = fontset.getGlyph(codePoint);
            var obfuscated = style.isObfuscated() && codePoint != 32;
            if (obfuscated) containsObfuscation = true;
            characters.add(new FontCharacter(
                    getTexture(bakedGlyph.renderType(Font.DisplayMode.NORMAL)),
                    fontset,
                    glyphinfo,
                    bakedGlyph,
                    r,
                    g,
                    b,
                    obfuscated,
                    bold,
                    style.isItalic(),
                    style.isStrikethrough(),
                    style.isUnderlined(),
                    x,
                    x + advance,
                    Mth.ceil(bold ? glyphinfo.getAdvance(false) : advance)
            ));
            x += advance;
            return true;
        }

        /**
         * Cleans up this output after it has received all inputs.
         */
        public void clean() {
            // Sort the characters by font so we draw them one font at a time, greatly reducing
            // buffer switches when combining different font types!
            characters.sort(Comparator.comparing(f -> f.texture));
        }

        /**
         * Finishes creation of this string and draws it to the given buffer.
         */
        public float finish(Font font, float x, float y, int color, boolean shadow, Matrix4f matrix, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int background, int packedLightCoords) {
            var right = x;
            var dimFactor = shadow ? 0.25F : 1.0F;
            var decorationOffset = shadow ? 1f : 0f;
            var r = (float) (color >> 16 & 255) / 255.0F * dimFactor;
            var g = (float) (color >> 8 & 255) / 255.0F * dimFactor;
            var b = (float) (color & 255) / 255.0F * dimFactor;
            var a = (float) (color >> 24 & 255) / 255.0F;
            var baseVertexconsumer = bufferSource.getBuffer(baseBakedGlyph.renderType(displayMode));

            for (FontCharacter character : this.characters) {
                var bakedglyph = character.obfuscated ? getRandomGlyph(character.fontSet, character.nonBoldAdvance) : character.bakedGlyph;

                if (!(bakedglyph instanceof EmptyGlyph)) {
                    var boldOffset = character.bold ? character.glyphInfo.getBoldOffset() : 0.0F;
                    var shadowOffset = shadow ? character.glyphInfo.getShadowOffset() : 0.0F;
                    var vertexconsumer = bufferSource.getBuffer(bakedglyph.renderType(displayMode));
                    font.renderChar(bakedglyph, character.bold, character.italic, boldOffset, x + character.left + shadowOffset, y + shadowOffset, matrix, vertexconsumer, character.r == null ? r : character.r * dimFactor, character.g == null ? g : character.g * dimFactor, character.b == null ? b : character.b * dimFactor, a, packedLightCoords);
                }

                if (character.strikethrough) {
                    render(baseBakedGlyph, x + decorationOffset - 1.0F, y + decorationOffset + 4.5F, x + character.left + decorationOffset, y + decorationOffset + 4.5F - 1.0F, 0.01F, character.r == null ? r : character.r * dimFactor, character.g == null ? g : character.g * dimFactor, character.b == null ? b : character.b * dimFactor, a, matrix, baseVertexconsumer, packedLightCoords);
                }
                if (character.underline) {
                    render(baseBakedGlyph, x + decorationOffset - 1.0F, y + decorationOffset + 9.0F, x + character.left + decorationOffset, y + decorationOffset + 9.0F - 1.0F, 0.01F, character.r == null ? r : character.r * dimFactor, character.g == null ? g : character.g * dimFactor, character.b == null ? b : character.b * dimFactor, a, matrix, baseVertexconsumer, packedLightCoords);
                }

                // Store the furthest right we have drawn!
                right = Math.max(right, character.right + x);
            }

            if (background != 0) {
                var br = (float) (background >> 24 & 255) / 255.0F;
                var bg = (float) (background >> 16 & 255) / 255.0F;
                var bb = (float) (background >> 8 & 255) / 255.0F;
                var ba = (float) (background & 255) / 255.0F;

                render(baseBakedGlyph, x - 1.0F, y + 9.0F, x + 1.0F, y - 1.0F, 0.01F, br, bg, bb, ba, matrix, baseVertexconsumer, packedLightCoords);
            }

            return right;
        }

        /**
         * Determines the texture based on the given type.
         */
        private ResourceLocation getTexture(RenderType type) {
            if (type instanceof RenderType.CompositeRenderType composite) {
                if (composite.state.textureState instanceof RenderType.CompositeRenderType.TextureStateShard textureShard) {
                    return (textureShard.texture.orElse(new ResourceLocation(""))).withSuffix("/" + type.name);
                }
            }
            return new ResourceLocation("/" + type.name);
        }

        /**
         * Returns a random glyph in the given font set with the given advance.
         */
        private BakedGlyph getRandomGlyph(FontSet fontSet, int advance) {
            IntList intList = fontSet.glyphsByWidth.get(advance);
            return intList != null && !intList.isEmpty() ? fontSet.getGlyph(intList.getInt(RANDOM.nextInt(intList.size()))) : fontSet.missingGlyph;
        }

        /**
         * Renders a new effect to the given vertex consumer.
         */
        private void render(BakedGlyph glyph, float x, float y, float x2, float y2, float depth, Float r, Float g, Float b, float a, Matrix4f matrix, VertexConsumer vertexConsumer, int i) {
            vertexConsumer.vertex(matrix, x, y, depth).color(r, g, b, a).uv(glyph.u0, glyph.v0).uv2(i).endVertex();
            vertexConsumer.vertex(matrix, x2, y, depth).color(r, g, b, a).uv(glyph.u0, glyph.v1).uv2(i).endVertex();
            vertexConsumer.vertex(matrix, x2, y2, depth).color(r, g, b, a).uv(glyph.u1, glyph.v1).uv2(i).endVertex();
            vertexConsumer.vertex(matrix, x, y2, depth).color(r, g, b, a).uv(glyph.u1, glyph.v0).uv2(i).endVertex();
        }

        /**
         * Stores details about a single character to draw.
         */
        public record FontCharacter(
                ResourceLocation texture,
                FontSet fontSet,
                GlyphInfo glyphInfo,
                BakedGlyph bakedGlyph,
                Float r,
                Float g,
                Float b,
                boolean obfuscated,
                boolean bold,
                boolean italic,
                boolean strikethrough,
                boolean underline,
                float left,
                float right,
                int nonBoldAdvance
        ) {
        }
    }
}
