package com.noxcrew.noxesium.skull;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * A custom chat component that renders a player's face at its location. The
 * input is directly received as a texture but can optionally be a unique id.
 */
public class SkullContents implements ComponentContents {

    private final String texture;
    private final int advance;
    private final int ascent;
    private final float scale;
    private final SkullConfig config;

    public SkullContents(String texture, int advance, int ascent, float scale) {
        this.texture = texture;
        this.advance = advance;
        this.ascent = ascent;
        this.scale = scale;
        this.config = new SkullConfig(texture, advance, ascent, scale);
    }

    public String getTexture() {
        return texture;
    }

    public int getAdvance() {
        return advance;
    }

    public int getAscent() {
        return ascent;
    }

    public float getScale() {
        return scale;
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) {
        return Component.literal(Character.toString(CustomSkullFont.claim(config))).setStyle(Style.EMPTY.withFont(CustomSkullFont.RESOURCE_LOCATION));
    }

    @Override
    public String toString() {
        return "skull{texture='" + texture + "', advance='" + advance + "', ascent='" + ascent + "', scale='" + scale + "'}";
    }
}
