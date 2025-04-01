package com.noxcrew.noxesium.mixin.rules.entity;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.entity.ExtraEntityData;
import java.awt.Color;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class CustomGlowColorRendererMixin {


    @Shadow public abstract boolean equals(Object object);

    @Inject(method = "getTeamColor", at = @At("RETURN"), cancellable = true)
    public void injectChangeColorValue(CallbackInfoReturnable<Integer> cir) {
        Entity entity = ((Entity)(Object)this);
        Player player = Minecraft.getInstance().player;

        NoxesiumMod.getInstance().getLogger().warn("[5] Glowing here!");

        if (player == null) {
            return;
        }

        NoxesiumMod.getInstance().getLogger().warn("[6] Glowing here!");

        Optional<Color> customGlowColor = entity.noxesium$getExtraData(ExtraEntityData.CUSTOM_GLOW_COLOR);

        NoxesiumMod.getInstance().getLogger().warn("[6.5] Entity has ID: " + entity.getId());
        NoxesiumMod.getInstance().getLogger().warn("[7] Custom Color is: " + customGlowColor);

        customGlowColor.ifPresent(color -> cir.setReturnValue(color.getRGB()));

        NoxesiumMod.getInstance().getLogger().warn("[8] Custom Color is: " + customGlowColor);
    }
}