package com.noxcrew.noxesium.fabric.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.fabric.feature.render.CustomMapUiWidget;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.client.resources.model.AtlasIds;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adds the map textures to the GUI atlas for usage in the overlay.
 */
@Mixin(SpriteSourceList.class)
public class AddMapSpritesToAtlas {
    @WrapOperation(
            method = "load",
            at =
                    @At(
                            value = "NEW",
                            target =
                                    "(Ljava/util/List;)Lnet/minecraft/client/renderer/texture/atlas/SpriteSourceList;"))
    private static SpriteSourceList buildSpriteList(
            List<SpriteSource> sources,
            Operation<SpriteSourceList> original,
            @Local(argsOnly = true) ResourceLocation atlas) {
        // Ignore atlases that are not the GUI one
        if (atlas != AtlasIds.GUI) {
            return original.call(sources);
        }

        // Add specifically the map backgrounds, but not the decorations!
        var newList = new ArrayList<>(sources);
        newList.add(new SingleFile(CustomMapUiWidget.MAP_BACKGROUND));
        newList.add(new SingleFile(CustomMapUiWidget.MAP_BACKGROUND_CHECKERBOARD));
        return original.call(newList);
    }
}
