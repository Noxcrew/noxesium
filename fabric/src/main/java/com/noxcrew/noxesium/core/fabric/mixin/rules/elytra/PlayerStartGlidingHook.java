package com.noxcrew.noxesium.core.fabric.mixin.rules.elytra;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.api.registry.GameComponents;
import com.noxcrew.noxesium.core.fabric.feature.entity.FallFlyingEntityExtension;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Redirects when a player wants to start gliding to the custom logic.
 */
@Mixin(Player.class)
public class PlayerStartGlidingHook {
    @WrapMethod(method = "startFallFlying")
    public void onStartGliding(Operation<Void> original) {
        if (!GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_ELYTRA))
            return;
        ((FallFlyingEntityExtension) this).noxesium$startFallFlying();
    }
}
