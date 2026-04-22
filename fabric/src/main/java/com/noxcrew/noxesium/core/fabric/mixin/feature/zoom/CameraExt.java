package com.noxcrew.noxesium.core.fabric.mixin.feature.zoom;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface CameraExt {
    @Accessor("fovModifier")
    float getFovModifier();
}
