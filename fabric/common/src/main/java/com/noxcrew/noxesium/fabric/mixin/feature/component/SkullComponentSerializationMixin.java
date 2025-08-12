package com.noxcrew.noxesium.fabric.mixin.feature.component;

import com.noxcrew.noxesium.fabric.feature.skull.SkullContents;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Modifies [Component] codecs to add support for skull components.
 */
@Mixin(ComponentSerialization.class)
public abstract class SkullComponentSerializationMixin {

    @ModifyVariable(method = "createCodec", at = @At(value = "STORE"), ordinal = 0)
    private static ComponentContents.Type<?>[] getTypes(ComponentContents.Type<?>[] types) {
        var newArray = new ComponentContents.Type[types.length + 1];
        System.arraycopy(types, 0, newArray, 0, types.length);
        newArray[types.length] = SkullContents.TYPE;
        return newArray;
    }
}
