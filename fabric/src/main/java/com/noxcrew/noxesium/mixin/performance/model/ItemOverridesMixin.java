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
 * This completely replaces vanilla's item overrides system as it's quite bad. Vanilla
 * supports tons of different overrides but treats them all completely equal. This is
 * quite bad considering the vast majority of overrides are single overrides based
 * on custom model data. Instead, we optimize based on the amount of overrides and
 * treat all non-only custom model data ones as non-essential.
 * <p>
 * Disabled when using <a href="https://github.com/emilyploszaj/chime/tree/main">Chime</a>.
 */
@Mixin(value = BlockModel.class, priority = 500)
public abstract class ItemOverridesMixin {
    /**
     * @author Aeltumn
     * @reason Vanilla overrides are incredibly slow, best to replace the whole system here
     */
    @Redirect(method = "getItemOverrides", at = @At(value = "NEW", target = "(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/List;)Lnet/minecraft/client/renderer/block/model/ItemOverrides;"))
    public ItemOverrides replaceItemOverrides(ModelBaker baker, BlockModel model, List<ItemOverride> overrides) {
        return new CustomItemOverrides(baker, model, overrides);
    }
}
