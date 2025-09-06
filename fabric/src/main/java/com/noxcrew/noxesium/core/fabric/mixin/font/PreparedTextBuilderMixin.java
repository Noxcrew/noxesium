package com.noxcrew.noxesium.core.fabric.mixin.font;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.core.util.OffsetStringFormatter;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixes in to fonts and reads an optional x, y rendering offset from the
 * style's insertion value.
 */
@Mixin(Font.PreparedTextBuilder.class)
public class PreparedTextBuilderMixin {

    @Shadow
    float x;

    @WrapOperation(
            method = "accept",
            at =
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/gui/Font$PreparedTextBuilder;x:F",
                            opcode = Opcodes.GETFIELD))
    public float redirectGetX(
            Font.PreparedTextBuilder instance, Operation<Float> original, @Local(argsOnly = true) Style style) {
        var offset = OffsetStringFormatter.parseX(style.getInsertion());
        if (offset != null) {
            return original.call(instance) + offset;
        }
        return original.call(instance);
    }

    @Inject(method = "accept", at = @At("TAIL"))
    public void fixXValue(int ignored1, Style style, int ignored2, CallbackInfoReturnable<Boolean> cir) {
        // The last line is this.x += advance which calls the getX() redirect which adds the offset,
        // so we need to reduce it by the offset to compensate.
        var offset = OffsetStringFormatter.parseX(style.getInsertion());
        if (offset != null) {
            this.x -= offset;
        }
    }

    @WrapOperation(
            method = "accept",
            at =
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/gui/Font$PreparedTextBuilder;y:F",
                            opcode = Opcodes.GETFIELD))
    public float redirectGetY(
            Font.PreparedTextBuilder instance, Operation<Float> original, @Local(argsOnly = true) Style style) {
        var offset = OffsetStringFormatter.parseY(style.getInsertion());
        if (offset != null) {
            return original.call(instance) + offset;
        }
        return original.call(instance);
    }
}
