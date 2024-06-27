package com.noxcrew.noxesium.mixin.model;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import static com.noxcrew.noxesium.NoxesiumMod.BUKKIT_COMPOUND_ID;
import static com.noxcrew.noxesium.NoxesiumMod.RAW_MODEL_TAG;

/**
 * Override the models used as a base for item stacks with a BLOCK_STATE component which can
 * be used to make them placed with specific orientations. This allows glazed terracotta blocks
 * to be turned into individual states.
 * <p>
 * This behavior is only used for items with a specific cache as to not affect default behavior.
 */
@Mixin(ItemModelShaper.class)
public class ItemModelShaperMixin {

    @Unique
    private BakedModel noxesium$getItemModel(ItemStack itemStack) {
        // Ignore items without block state data!
        if (!itemStack.has(DataComponents.BLOCK_STATE)) return null;

        // Ignore items that cannot render as blocks
        if (!(itemStack.getItem() instanceof BlockItem blockItem)) return null;

        // Ignore items without the raw model flag!
        final CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        final CompoundTag tag = data.getUnsafe();
        if (tag == null) return null;
        final CompoundTag bukkit = tag.getCompound(BUKKIT_COMPOUND_ID);
        if (!bukkit.contains(RAW_MODEL_TAG)) return null;

        // Determine the state to show and render its model through the block model cache
        final var state = itemStack.get(DataComponents.BLOCK_STATE);
        final var blockState = state.apply(blockItem.getBlock().defaultBlockState());
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);
    }

    @WrapMethod(method = "getItemModel(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/BakedModel;")
    public BakedModel getItemModel(ItemStack itemStack, Operation<BakedModel> original) {
        var model = noxesium$getItemModel(itemStack);
        return model != null ? model : original.call(itemStack);
    }
}
