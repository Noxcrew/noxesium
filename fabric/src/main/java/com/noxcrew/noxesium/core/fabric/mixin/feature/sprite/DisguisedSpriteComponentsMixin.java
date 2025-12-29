package com.noxcrew.noxesium.core.fabric.mixin.feature.sprite;

import com.noxcrew.noxesium.api.feature.sprite.CustomSkullSprite;
import com.noxcrew.noxesium.api.feature.sprite.CustomSpriteRegistry;
import com.noxcrew.noxesium.core.fabric.feature.sprite.DisguisedSpriteTranslationContents;
import com.noxcrew.noxesium.core.fabric.feature.sprite.SkullSprite;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hijacks the translation component to allow including custom sprites directly
 * in a way that supports falling back to another value if the client is not
 * using Noxesium.
 */
@Mixin(TranslatableContents.class)
public abstract class DisguisedSpriteComponentsMixin {

    /**
     * @author Aeltumn
     * @reason Allow defining a DisguisedSpriteTranslationContents object.
     */
    @Inject(method = "create", at = @At("RETURN"), cancellable = true)
    private static void createSkullContents(
            String string,
            Optional<String> optional,
            Optional<List<Object>> optional2,
            CallbackInfoReturnable<TranslatableContents> cir) {
        var type = CustomSpriteRegistry.parse(string);
        if (type == null) return;

        // Create the sprite based on the custom type
        ObjectInfo sprite;
        if (type instanceof CustomSkullSprite skull) {
            UUID uuid = null;
            String texture = null;
            if (skull.raw()) {
                // If raw we load the texture directly
                texture = skull.value();
            } else {
                // If it's a uuid we create a task to fetch it and complete later
                var stringUuid = skull.value();
                try {
                    uuid = UUID.fromString(stringUuid);
                } catch (Exception ignored) {
                }
            }
            sprite = new SkullSprite(
                    Optional.ofNullable(uuid),
                    Optional.ofNullable(texture),
                    skull.advance(),
                    skull.ascent(),
                    skull.scale(),
                    skull.hat());
        } else {
            throw new RuntimeException("Unknown sprite type '" + type.type() + "'");
        }

        // Use the regular return value for serialization!
        cir.setReturnValue(new DisguisedSpriteTranslationContents(sprite, cir.getReturnValue()));
    }
}
