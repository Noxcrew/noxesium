package com.noxcrew.noxesium.mixin.model;

import com.noxcrew.noxesium.feature.model.CustomItemOverrides;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.ModelBaker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

/**
 * This completely replaces vanilla's item overrides system as it's quite bad. Vanilla
 * supports tons of different overrides but treats them all completely equal. This is
 * quite bad considering the vast majority of overrides are single overrides based
 * on custom model data. Instead, we optimize based on the amount of overrides and
 * treat all non-only custom model data ones as non-essential.
 */
@Mixin(BlockModel.class)
public class ItemOverridesMixin {

    @Shadow
    @Final
    private List<ItemOverride> overrides;

    /**
     * @author Aeltumn
     * @reason Vanilla overrides are incredibly slow, best to replace the whole system here
     */
    @Overwrite
    public ItemOverrides getItemOverrides(ModelBaker baker, BlockModel model) {
        // Re-use the same empty instance whenever possible
        return this.overrides.isEmpty() ? CustomItemOverrides.EMPTY : new CustomItemOverrides(baker, model, this.overrides);
    }
}
