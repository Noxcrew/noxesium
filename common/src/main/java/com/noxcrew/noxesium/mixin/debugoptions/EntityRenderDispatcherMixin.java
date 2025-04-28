package com.noxcrew.noxesium.mixin.debugoptions;

import com.noxcrew.noxesium.api.util.DebugOption;
import com.noxcrew.noxesium.feature.rule.ServerRules;
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
        if (renderHitBoxes
                && ServerRules.RESTRICT_DEBUG_OPTIONS != null
                && ServerRules.RESTRICT_DEBUG_OPTIONS.getValue().contains(DebugOption.SHOW_HITBOXES.getKeyCode())) {
            cir.setReturnValue(false);
        }
    }
}
