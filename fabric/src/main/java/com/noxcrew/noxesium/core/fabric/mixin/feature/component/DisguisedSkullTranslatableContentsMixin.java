package com.noxcrew.noxesium.core.fabric.mixin.feature.component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.noxcrew.noxesium.core.fabric.feature.skull.FakeTranslationContents;
import com.noxcrew.noxesium.core.fabric.feature.skull.SkullSprite;
import com.noxcrew.noxesium.core.util.SkullStringFormatter;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hijacks the translation component to allow including skull sprites directly
 * in a way that supports falling back to another value if the client is not
 * using Noxesium.
 */
@Mixin(TranslatableContents.class)
public abstract class DisguisedSkullTranslatableContentsMixin {

    /**
     * @author Aeltumn
     * @reason Allow defining a DisguisedSkullContents object.
     */
    @Inject(method = "create", at = @At("RETURN"), cancellable = true)
    private static void createSkullContents(
            String string,
            Optional<String> optional,
            Optional<List<Object>> optional2,
            CallbackInfoReturnable<TranslatableContents> cir) {
        // We allow custom servers to use a custom translate component since it renders as the fallback if the value is
        // not found.
        if (string.startsWith("%nox_uuid%") || string.startsWith("%nox_raw%")) {
            try {
                var info = SkullStringFormatter.parse(string);
                UUID uuid = null;
                String texture = null;
                if (info.raw()) {
                    // If raw we load the texture directly
                    texture = info.value();
                } else {
                    // If it's a uuid we create a task to fetch it and complete later
                    var stringUuid = info.value();
                    try {
                        uuid = UUID.fromString(stringUuid);
                    } catch (Exception ignored) {
                    }
                }
                cir.setReturnValue(new FakeTranslationContents(
                        new SkullSprite(
                                Optional.ofNullable(uuid),
                                Optional.ofNullable(texture),
                                info.grayscale(),
                                info.advance(),
                                info.ascent(),
                                info.scale(),
                                info.hat()),
                        // Use the regular return value for serialization!
                        cir.getReturnValue()));
            } catch (Exception ignored) {
            }
        }
    }
}
