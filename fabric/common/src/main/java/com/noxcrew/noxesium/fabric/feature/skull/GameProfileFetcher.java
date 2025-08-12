package com.noxcrew.noxesium.fabric.feature.skull;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class GameProfileFetcher {

    public static final String PROPERTY_TEXTURES = "textures";

    /**
     * Fills in the incomplete parts of the given profile, given the uuid is present.
     */
    public static void updateGameProfile(@Nullable GameProfile profile, Consumer<GameProfile> consumer) {
        // If anything is missing or we already have data we just keep what we have.
        if (profile == null || profile.getId() == null) {
            consumer.accept(profile);
            return;
        }

        CompletableFuture.runAsync(
                () -> {
                    // Try to use the session service to fill out the data, but otherwise we just use what we have
                    var newProfile = profile;
                    Property property =
                            Iterables.getFirst(newProfile.getProperties().get(PROPERTY_TEXTURES), null);
                    if (property == null) {
                        newProfile = Minecraft.getInstance()
                                .getMinecraftSessionService()
                                .fetchProfile(newProfile.getId(), true)
                                .profile();
                    }
                    consumer.accept(newProfile);
                },
                Util.backgroundExecutor());
    }
}
