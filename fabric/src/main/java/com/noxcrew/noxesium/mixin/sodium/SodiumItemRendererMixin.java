package com.noxcrew.noxesium.mixin.sodium;

import com.noxcrew.noxesium.feature.model.ItemColorWrapper;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import me.jellysquid.mods.sodium.client.model.color.interop.ItemColorsExtended;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// Set a priority higher than Sodium
@Mixin(value = ItemColors.class, priority = 1001)
public class SodiumItemRendererMixin implements ItemColorsExtended {

    // This field is created by Sodium in ItemColorsMixin
    @Shadow
    @Final
    private Reference2ReferenceMap<ItemLike, ItemColor> itemsToColor;

    /**
     * @reason Cache color provider lookups, more details in ItemColorWrapper docs
     * @author Aeltumn
     */
    @Override
    public ItemColor sodium$getColorProvider(ItemStack stack) {
        var wrapped = this.itemsToColor.get(stack.getItem());
        return wrapped == null ? null : new ItemColorWrapper(wrapped);
    }
}
