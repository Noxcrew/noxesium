package com.noxcrew.noxesium.core.fabric.mixin.rules;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.registry.GameComponents;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hooks into the auto spin attack and optionally disables its collision.
 */
@Mixin(LivingEntity.class)
public abstract class SpinAttackCollisionMixin {

    @WrapOperation(
            method = "checkAutoSpinAttack",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
    private List<Entity> checkServerForSpinCollision(
            Level instance, Entity entity, AABB aabb, Operation<List<Entity>> original) {
        // If the spin attack is disabled we return no targets which makes the client
        // think there is nothing to collide with.
        if (GameComponents.getInstance()
                .noxesium$hasComponent(CommonGameComponentTypes.DISABLE_SPIN_ATTACK_COLLISIONS)) {
            return List.of();
        }
        return original.call(instance, entity, aabb);
    }
}
