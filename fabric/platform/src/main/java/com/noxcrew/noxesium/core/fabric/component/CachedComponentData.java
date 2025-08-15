package com.noxcrew.noxesium.core.fabric.component;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores a component type and its de-serialized object.
 */
public class CachedComponentData<T> {
    private final Tag tag;
    private @Nullable Codec<T> codec;
    private @Nullable T object;

    public CachedComponentData(Tag tag, @NotNull Codec<T> codec) {
        this.tag = tag;
        this.codec = codec;
    }

    /**
     * Loads this component's data.
     */
    @Nullable
    public T load() {
        if (codec == null) return object;
        var result = codec.decode(NbtOps.INSTANCE, tag);
        if (result.hasResultOrPartial()) {
            object = result.resultOrPartial().map(Pair::getFirst).orElse(null);
        }
        codec = null;
        return object;
    }
}
