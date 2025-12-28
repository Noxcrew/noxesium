package com.noxcrew.noxesium.core.fabric.mixin.settings;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes mouse button down states not being updated when a screen is closed.
 * Keyboard buttons continuously emit events while held so we need to update
 * whether mouse buttons are held whenever we leave a screen as they will have
 * become desynced while the menu was open, and we released all when opening.
 * <p>
 * MC-301281 was marked WAI because mouse buttons are not kept even in
 * hold mode if you toggle a menu, so using this unreported bug as a precedent.
 * However, keyboard buttons don't work like this ever, so we have to fix
 * mouse buttons in both toggle and hold mode.
 */
@Mixin(KeyMapping.class)
public abstract class FixMouseDownAfterScreenClose {
    @Shadow
    @Final
    private static Map<String, KeyMapping> ALL;

    @Inject(method = "restoreToggleStatesOnScreenClosed", at = @At("HEAD"))
    private static void onScreenClosed(CallbackInfo ci) {
        var window = Minecraft.getInstance().getWindow();
        for (var keymapping : ALL.values()) {
            if (keymapping.key.getType() != InputConstants.Type.MOUSE) continue;
            keymapping.setDown(GLFW.glfwGetMouseButton(window.handle(), keymapping.key.getValue()) == 1);
        }
    }
}
