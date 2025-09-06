package com.noxcrew.noxesium.example.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.api.component.NoxesiumComponentHolder;
import com.noxcrew.noxesium.example.ExampleBlockEntityComponents;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Makes the coolest block entities invisible.
 */
@Mixin(BlockEntityRenderer.class)
public interface BlockEntityRendererMixin {

    @WrapMethod(method = "shouldRender")
    private boolean render(BlockEntity blockEntity, Vec3 vec3, Operation<Boolean> original) {
        if (((NoxesiumComponentHolder) blockEntity).noxesium$hasComponent(ExampleBlockEntityComponents.INVISIBLE))
            return false;
        return original.call(blockEntity, vec3);
    }
}
