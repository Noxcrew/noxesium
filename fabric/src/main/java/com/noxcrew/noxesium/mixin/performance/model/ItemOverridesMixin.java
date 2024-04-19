package com.noxcrew.noxesium.mixin.performance.model;

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
 * Replaces vanilla custom item overrides whenever just custom model
 * data values are being used as we can replace vanilla's for-loop
 * implementation with a far more efficient map lookup.
 */
@Mixin(BlockModel.class)
public class ItemOverridesMixin {

    @Shadow
    @Final
    private List<ItemOverride> overrides;

    /**
     * @author Aeltumn
     * @reason Replace item overrides that solely contain custom model data values
     */
    @Overwrite
    public ItemOverrides getItemOverrides(ModelBaker baker, BlockModel model) {
        return this.overrides.isEmpty() ? ItemOverrides.EMPTY : new CustomItemOverrides(baker, model, this.overrides);
    }
}
