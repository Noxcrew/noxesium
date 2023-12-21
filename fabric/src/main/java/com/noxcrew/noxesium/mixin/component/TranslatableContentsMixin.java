package com.noxcrew.noxesium.mixin.component;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.noxcrew.noxesium.api.protocol.skull.SkullStringFormatter;
import com.noxcrew.noxesium.feature.skull.GameProfileFetcher;
import com.noxcrew.noxesium.feature.skull.SkullContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Modifies [Component] codecs to add support for skull components.
 */
@Mixin(TranslatableContents.class)
public abstract class TranslatableContentsMixin {

    /**
     * @author Aeltumn
     * @reason Allow defining a DisguisedSkullContents object.
     */
    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void createSkullContents(String string, Optional<String> optional, Optional<List<Object>> optional2, CallbackInfoReturnable<TranslatableContents> cir) {
        // We allow custom servers to use a custom translate component since it renders as the fallback if the value is not found.
        if (string.startsWith("%nox_uuid%") || string.startsWith("%nox_raw%")) {
            var info = SkullStringFormatter.parse(string);
            try {
                UUID uuid = null;
                CompletableFuture<String> texture = new CompletableFuture<>();
                if (info.raw()) {
                    // If raw we load the texture directly
                    texture.complete(info.value());
                } else {
                    // If it's a uuid we create a task to fetch it and complete later
                    var stringUuid = info.value();
                    try {
                        uuid = UUID.fromString(stringUuid);
                        GameProfile gameprofile = new GameProfile(uuid, "dummy_mcdummyface");
                        GameProfileFetcher.updateGameProfile(gameprofile, (profile) -> {
                            var property = Iterables.getFirst(profile.getProperties().get(GameProfileFetcher.PROPERTY_TEXTURES), null);
                            if (property != null) {
                                texture.complete(property.value());
                            }
                        });
                    } catch (Exception ignored) {}
                }
                cir.setReturnValue(new SkullContents(uuid, texture, info.grayscale(), info.advance(), info.ascent(), info.scale()));
            } catch (Exception ignored) {}
        }
    }
}
