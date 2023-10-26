package com.noxcrew.noxesium.feature.render;

import com.noxcrew.noxesium.feature.render.cache.ScoreboardCache;
import com.noxcrew.noxesium.feature.render.cache.ScoreboardInformation;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Listens to Minecraft reloading the resources and clears cached scoreboard information as the
 * contents of the resource pack may have changed.
 */
public class NoxesiumReloadListener implements SimpleSynchronousResourceReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        ScoreboardCache.getInstance().clearCache();
    }

    @Override
    public String getName() {
        return "noxesium";
    }

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation("noxesium", "reload");
    }
}
