package com.noxcrew.noxesium.mixin.settings;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.NoxesiumMod;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

/**
 * Hooks into getting tags and reports if any don't exist.
 */
@Mixin(MappedRegistry.class)
public class MissingTagExceptionMixin<T> {

    @WrapMethod(method = "getTag")
    public Optional<HolderSet.Named<T>> getTag(TagKey<T> tagKey, Operation<Optional<HolderSet.Named<T>>> original) {
        var result = original.call(tagKey);
        if (result.isEmpty() && NoxesiumMod.getInstance().getConfig().printPacketExceptions) {
            NoxesiumMod.getInstance().getLogger().error("Tried to fetch tag named {} from {} but it was not found", tagKey.location(), tagKey.registry());
        }
        return result;
    }
}
