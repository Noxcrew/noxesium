package com.noxcrew.noxesium.core.fabric.mixin.debugoptions;

import com.noxcrew.noxesium.api.client.DebugOption;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Shadow
    private boolean renderHitBoxes;

    @Inject(method = "shouldRenderHitBoxes", at = @At("HEAD"), cancellable = true)
    private void restrictHitBoxRendering(CallbackInfoReturnable<Boolean> cir) {
        if (renderHitBoxes) {
            var restrictedOptions =
                    Minecraft.getInstance().noxesium$getComponent(CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS);
            if (restrictedOptions != null && restrictedOptions.contains(DebugOption.SHOW_HITBOXES.getKeyCode())) {
                cir.setReturnValue(false);
            }
        }
    }
}
