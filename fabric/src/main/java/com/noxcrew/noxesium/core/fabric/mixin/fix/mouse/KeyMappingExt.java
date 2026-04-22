package com.noxcrew.noxesium.core.fabric.mixin.fix.mouse;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyMappingExt {
    @Accessor("key")
    InputConstants.Key getKey();
}
