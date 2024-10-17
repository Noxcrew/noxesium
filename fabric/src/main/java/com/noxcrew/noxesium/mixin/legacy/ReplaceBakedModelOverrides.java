package com.noxcrew.noxesium.mixin.legacy;

import com.noxcrew.noxesium.feature.model.CustomBakedOverrides;
import net.minecraft.client.renderer.block.model.BakedOverrides;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.resources.model.ItemModel;
import net.minecraft.client.resources.model.ModelBaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Replaces vanilla custom item overrides whenever just CustomModelData is being used
 * to replace iterator over all overrides with a direct lookup. This greatly improves
 * performance when single items have large amounts of overrides.
 * <p>
 * Will be removed in a future version as the model component now exists which should
 * be used if possible as it great performance for all clients.
 */
@Mixin(value = ItemModel.class, priority = 500)
@Deprecated
public abstract class ReplaceBakedModelOverrides {

    /**
     * @author Aeltumn
     * @reason Replace item overrides that solely contain custom model data values
     */
    @Redirect(method = "bake", at = @At(value = "NEW", target = "(Lnet/minecraft/client/resources/model/ModelBaker;Ljava/util/List;)Lnet/minecraft/client/renderer/block/model/BakedOverrides;"))
    public BakedOverrides replaceItemOverrides(ModelBaker modelBaker, List<ItemOverride> overrides) {
        return new CustomBakedOverrides(modelBaker, overrides);
    }
}
