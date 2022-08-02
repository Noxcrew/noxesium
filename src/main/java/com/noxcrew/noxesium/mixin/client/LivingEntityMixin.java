package com.noxcrew.noxesium.mixin.client;

import com.noxcrew.noxesium.rule.ServerRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Hooks into the auto spin attack and optionally disables its collision.
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Redirect(method = "checkAutoSpinAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
    private List<Entity> injected(Level instance, Entity entity, AABB aabb) {
        // If the spin attack is disabled we return no targets which makes the client
        // think there is nothing to collide with.
        // This server rule could be replaced with a GameRule.
        if (ServerRules.DISABLE_AUTO_SPIN_ATTACK.get()) {
            return List.of();
        }
        return instance.getEntities(entity, aabb);
    }
}
