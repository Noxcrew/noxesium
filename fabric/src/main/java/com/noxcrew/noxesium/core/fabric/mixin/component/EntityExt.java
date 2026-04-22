package com.noxcrew.noxesium.core.fabric.mixin.component;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityExt {
    @Invoker("makeBoundingBox")
    AABB invokeMakeBoundingBox();
}
