package com.noxcrew.noxesium.core.fabric.mixin.rules.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import java.awt.Color;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class CustomGlowColorRendererMixin {

    @Shadow
    public abstract boolean equals(Object object);

    @ModifyReturnValue(method = "getTeamColor", at = @At("RETURN"))
    public int injectChangeColorValue(int original) {
        Entity entity = ((Entity) (Object) this);
        var component = entity.noxesium$getOptionalComponent(CommonEntityComponentTypes.GLOW_COLOR);
        return component.map(Color::getRGB).orElse(original);
    }
}
