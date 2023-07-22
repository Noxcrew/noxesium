package com.noxcrew.noxesium.mixin.model;

import com.noxcrew.noxesium.feature.model.ItemColorWrapper;
import me.jellysquid.mods.sodium.client.world.biome.ItemColorsExtended;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Set a priority higher than Sodium
@Mixin(value = ItemRenderer.class, priority = 1001)
public class SodiumMixinItemRendererMixin {

    /**
     * @reason Cache color provider lookups, more details in ItemColorWrapper docs
     * @author Aeltumn
     */
    @Redirect(method = "renderModelLists", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/world/biome/ItemColorsExtended;getColorProvider(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/color/item/ItemColor;"))
    private ItemColor getItemColorProvider(ItemColorsExtended instance, ItemStack stack) {
        var wrapped = instance.getColorProvider(stack);
        return wrapped == null ? null : new ItemColorWrapper(wrapped);
    }
}
