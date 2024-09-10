package com.noxcrew.noxesium.mixin.legacy;

import com.noxcrew.noxesium.feature.model.CustomItemOverrides;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.ModelBaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Replaces vanilla custom item overrides whenever just CustomModelData is being used
 * to replace iterator over all overrides with a direct lookup. This greatly improves
 * performance when single items have large amounts of overrides.
 *
 * Will be removed in a future version as the model component now exists which should
 * be used if possible as it great performance for all clients.
 */
@Mixin(value = BlockModel.class, priority = 500)
@Deprecated
public abstract class CustomItemOverridesMixin {

    /**
     * @author Aeltumn
     * @reason Replace item overrides that solely contain custom model data values
     */
    @Redirect(method = "getItemOverrides", at = @At(value = "NEW", target = "(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/List;)Lnet/minecraft/client/renderer/block/model/ItemOverrides;"))
    public ItemOverrides replaceItemOverrides(ModelBaker baker, BlockModel model, List<ItemOverride> overrides) {
        return new CustomItemOverrides(baker, model, overrides);
    }
}
