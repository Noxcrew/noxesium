package com.noxcrew.noxesium.core.fabric.feature.sprite;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Hides a fake object sprite under a translation contents.
 */
public class DisguisedSpriteTranslationContents extends TranslatableContents {
    private static final String PLACEHOLDER = Character.toString('\ufffc');
    private final ObjectInfo sprite;

    public DisguisedSpriteTranslationContents(ObjectInfo sprite, TranslatableContents serialized) {
        // Ensure that the serialized object is still the same!
        super(serialized.getKey(), serialized.getFallback(), serialized.getArgs());
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
    public MutableComponent resolve(@Nullable CommandSourceStack p_237512_, @Nullable Entity p_237513_, int p_237514_) {
        // Don't change the component at all when resolving!
        return MutableComponent.create(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DisguisedSpriteTranslationContents that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(sprite, that.sprite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sprite);
    }
}
