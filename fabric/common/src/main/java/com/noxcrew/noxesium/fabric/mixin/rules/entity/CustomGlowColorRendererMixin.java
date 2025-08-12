package com.noxcrew.noxesium.fabric.mixin.rules.entity;

import com.noxcrew.noxesium.fabric.feature.entity.ExtraEntityData;
import java.awt.Color;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        Optional<Color> customGlowColor = entity.noxesium$getExtraData(ExtraEntityData.CUSTOM_GLOW_COLOR);
        customGlowColor.ifPresent(color -> cir.setReturnValue(color.getRGB()));
    }
}
