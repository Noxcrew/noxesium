package com.noxcrew.noxesium.feature.render;

import com.noxcrew.noxesium.feature.render.cache.actionbar.ActionBarCache;
import com.noxcrew.noxesium.feature.render.cache.chat.ChatCache;
import com.noxcrew.noxesium.feature.render.cache.scoreboard.ScoreboardCache;
import com.noxcrew.noxesium.feature.render.cache.bossbar.BossBarCache;
import com.noxcrew.noxesium.feature.render.cache.tablist.TabListCache;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Listens to Minecraft reloading the resources and clears cached scoreboard information as the
 * contents of the resource pack may have changed.
 */
public class NoxesiumReloadListener implements SimpleSynchronousResourceReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        ScoreboardCache.getInstance().clearCache();
        BossBarCache.getInstance().clearCache();
        ActionBarCache.getInstance().clearCache();
        TabListCache.getInstance().clearCache();
        ChatCache.getInstance().clearCache();
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
