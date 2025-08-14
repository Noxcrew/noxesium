package com.noxcrew.noxesium.core.fabric.mixin.rules.entity;

import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class CustomGlowColorRendererMixin {

    @Shadow
    public abstract boolean equals(Object object);

    @Inject(method = "getTeamColor", at = @At("RETURN"), cancellable = true)
    public void injectChangeColorValue(CallbackInfoReturnable<Integer> cir) {
        Entity entity = ((Entity) (Object) this);
        entity.noxesium$getOptionalComponent(CommonEntityComponentTypes.GLOW_COLOR)
                .ifPresent(color -> cir.setReturnValue(color.getRGB()));
    }
}
