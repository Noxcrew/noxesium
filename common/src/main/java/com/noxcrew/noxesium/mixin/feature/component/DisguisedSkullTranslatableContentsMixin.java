package com.noxcrew.noxesium.mixin.feature.component;

import com.noxcrew.noxesium.api.protocol.skull.SkullStringFormatter;
import com.noxcrew.noxesium.feature.skull.FakeTranslationContents;
import com.noxcrew.noxesium.feature.skull.SkullSprite;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void createSkullContents(
            String string,
            Optional<String> optional,
            Optional<List<Object>> optional2,
            CallbackInfoReturnable<TranslatableContents> cir) {
        // We allow custom servers to use a custom translate component since it renders as the fallback if the value is
        // not found.
        if (string.startsWith("%nox_uuid%") || string.startsWith("%nox_raw%")) {
            var info = SkullStringFormatter.parse(string);
            try {
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
                cir.setReturnValue(new FakeTranslationContents(new SkullSprite(
                        Optional.ofNullable(uuid),
                        Optional.ofNullable(texture),
                        info.grayscale(),
                        info.advance(),
                        info.ascent(),
                        info.scale(),
                        info.hat())));
            } catch (Exception ignored) {
            }
        }
    }
}
