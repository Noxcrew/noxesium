package com.noxcrew.noxesium.feature.skull;

import java.util.concurrent.CompletableFuture;
import net.minecraft.world.entity.player.PlayerSkin;

/**
 * A holding object for the texture to show for a skull and its properties.
 */
public record SkullConfig(CompletableFuture<PlayerSkin> texture, SkullProperties properties) {}
