package com.noxcrew.noxesium.feature.ui;

import com.noxcrew.noxesium.feature.ui.wrapper.ElementManager;
import com.noxcrew.noxesium.feature.ui.wrapper.ElementWrapper;
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
        ElementManager.getAllWrappers().forEach(ElementWrapper::requestRedraw);
    }

    @Override
    public String getName() {
        return "noxesium";
    }

    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath("noxesium", "reload");
    }
}
