package com.noxcrew.noxesium.mixin.client;

import net.minecraft.client.resources.model.ModelBakery;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Removes log spam about missing textures called #missing.
 */
@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    @Redirect(method = "method_24149", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private static void onEquals(Logger instance, String s, Object o, Object o2) {
        // Don't report #missing textures
        if (o instanceof String s2) {
            if (s2.equals("#missing")) return;
        }
        instance.warn(s, o, o2);
    }
}
