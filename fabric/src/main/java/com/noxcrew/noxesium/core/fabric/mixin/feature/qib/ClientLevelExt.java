package com.noxcrew.noxesium.core.fabric.mixin.feature.qib;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientLevel.class)
public interface ClientLevelExt {
    @Invoker("getEntities")
    LevelEntityGetter<Entity> invokeGetEntities();
}
