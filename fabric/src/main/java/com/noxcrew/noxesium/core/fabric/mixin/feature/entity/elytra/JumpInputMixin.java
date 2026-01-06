package com.noxcrew.noxesium.core.fabric.mixin.feature.entity.elytra;

import java.util.Objects;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Send a signal to the player class if the jump key is pressed.
 */
@Mixin(KeyMapping.class)
public class JumpInputMixin {
    @Shadow
    private boolean isDown;

    @Inject(method = "setDown", at = @At("HEAD"))
    public void onSetDown(boolean bl, CallbackInfo ci) {
        if (bl && !isDown && Objects.equals(this, Minecraft.getInstance().options.keyJump)) {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.noxesium$handleJump();
            }
        }
    }
}
