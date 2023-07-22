package com.noxcrew.noxesium.mixin.component;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SkullBlockEntity.class)
public interface SkullBlockEntityExt {

    @Accessor("sessionService")
    static MinecraftSessionService getSessionService() {
        throw new RuntimeException("Method is not callable");
    }
}
