package com.noxcrew.noxesium.core.fabric.mixin.feature.debugoptions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.core.feature.DebugOption;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adds various debug hotkeys for Noxesium.
 */
@Mixin(Minecraft.class)
public abstract class RestrictMoreDebugHotkeysMixin {

    @Shadow
    @Final
    public Gui gui;

    @WrapOperation(
            method = "handleKeybinds",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z", opcode = Opcodes.PUTFIELD))
    public void preventHidingGui(Options instance, boolean value, Operation<Void> original) {
        var restrictedOptions =
                GameComponents.getInstance().noxesium$getComponent(CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS);
        if (restrictedOptions != null && restrictedOptions.contains(DebugOption.HIDE_UI.getKeyCode())) {
            gui.getChat()
                    .addClientSystemMessage(Component.translatable("debug.warning.option.disabled")
                            .withStyle(ChatFormatting.RED));
            return;
        }
        original.call(instance, value);
    }
}
