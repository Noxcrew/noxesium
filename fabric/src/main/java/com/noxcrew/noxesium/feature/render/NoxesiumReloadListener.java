package com.noxcrew.noxesium.feature.render;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Listens to Minecraft reloading the resources and clears cached scoreboard information as the
 * contents of the resource pack may have changed.
 */
public class NoxesiumReloadListener implements PreparableReloadListener {

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        CachedScoreboardContents.clearCache();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "noxesium";
    }
}
