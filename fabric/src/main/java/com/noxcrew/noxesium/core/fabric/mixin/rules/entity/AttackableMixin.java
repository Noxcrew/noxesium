package com.noxcrew.noxesium.core.fabric.mixin.rules.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adds the ability to override attackability of entities.
 */
@Mixin(Player.class)
public class AttackableMixin {

    @WrapOperation(
            method = "cannotAttack",
            at =
            @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;isAttackable()Z"))
    public boolean redirectRenderHitbox(Entity instance, Operation<Boolean> original) {
        return instance.noxesium$getOptionalComponent(CommonEntityComponentTypes.ATTACKABLE)
                .orElseGet(() -> original.call(instance));
    }
}
