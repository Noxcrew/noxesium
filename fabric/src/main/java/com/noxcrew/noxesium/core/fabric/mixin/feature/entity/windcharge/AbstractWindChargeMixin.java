package com.noxcrew.noxesium.core.fabric.mixin.feature.entity.windcharge;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractWindCharge.class)
public abstract class AbstractWindChargeMixin extends AbstractHurtingProjectile implements ItemSupplier {

    protected AbstractWindChargeMixin(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    protected abstract void explode(Vec3 vec3);

    @Redirect(method = "onHitBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"))
    public boolean onHitBlock(Level instance) {
        if (GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_WINDCHARGES)) {
            return false;
        } else {
            return instance.isClientSide();
        }
    }

    @Inject(method = "onHitEntity", at = @At(value = "TAIL"))
    public void onHitEntity(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_WINDCHARGES)) {
            this.explode(this.position());
            this.discard();
        }
    }
}
