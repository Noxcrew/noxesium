package com.noxcrew.noxesium.mixin.performance.model;

import com.noxcrew.noxesium.feature.model.CustomItemOverrides;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.ModelBaker;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Replaces vanilla custom item overrides whenever just custom model
 * data values are being used as we can replace vanilla's for-loop
 * implementation with a far more efficient map lookup.
 */
@Mixin(value = BlockModel.class, priority = 500)
public abstract class ItemOverridesMixin {

    /**
     * @author Aeltumn
     * @reason Replace item overrides that solely contain custom model data values
     */
    @Redirect(method = "getItemOverrides", at = @At(value = "NEW", target = "(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/List;)Lnet/minecraft/client/renderer/block/model/ItemOverrides;"))
    public ItemOverrides replaceItemOverrides(ModelBaker baker, BlockModel model, List<ItemOverride> overrides) {
        return new CustomItemOverrides(baker, model, overrides);
    }
}
