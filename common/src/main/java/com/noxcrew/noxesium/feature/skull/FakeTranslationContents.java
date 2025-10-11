package com.noxcrew.noxesium.feature.skull;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Hides a fake skull sprite under a translation contents.
 */
public class FakeTranslationContents extends TranslatableContents {
    private static final String PLACEHOLDER = Character.toString('\ufffc');
    private final SkullSprite sprite;

    public FakeTranslationContents(SkullSprite sprite) {
        super("", null, new Object[] {});
        this.sprite = sprite;
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        return contentConsumer.accept(sprite.description());
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        return styledContentConsumer.accept(style.withFont(sprite.fontDescription()), PLACEHOLDER);
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) {
        return Component.literal(PLACEHOLDER).setStyle(Style.EMPTY.withFont(sprite.fontDescription()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FakeTranslationContents that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(sprite, that.sprite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sprite);
    }
}
