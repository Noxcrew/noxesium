package com.noxcrew.noxesium.mixin.debugoptions;

import com.noxcrew.noxesium.api.util.DebugOption;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(
            method =
                    "extractHitboxes(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V",
            at = @At("HEAD"),
            cancellable = true)
    private <T extends Entity, S extends EntityRenderState> void extractHitboxes(
            T entity, S state, float p_412603_, CallbackInfo ci) {
        if (ServerRules.RESTRICT_DEBUG_OPTIONS != null
                && ServerRules.RESTRICT_DEBUG_OPTIONS.getValue().contains(DebugOption.SHOW_HITBOXES.getKeyCode())) {
            ci.cancel();

            // Ensure the hitboxes are removed!
            state.hitboxesRenderState = null;
            state.serverHitboxesRenderState = null;
        }
    }
}
