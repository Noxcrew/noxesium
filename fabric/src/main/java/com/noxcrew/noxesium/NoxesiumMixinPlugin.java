package com.noxcrew.noxesium;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Enables certain mixins based on whether other mods are in use or not.
 */
public class NoxesiumMixinPlugin implements IMixinConfigPlugin {

    private boolean isUsingSodium, isUsingIris, isUsingChime;

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return switch (mixinClassName) {
            // Enable custom sodium compatibility for the beacon performance changes, but disable when
            // using iris as it makes changes that provide better performance
            case "com.noxcrew.noxesium.mixin.beacon.SodiumWorldRendererMixin" -> isUsingSodium && !isUsingIris;
            case "com.noxcrew.noxesium.mixin.performance.model.SodiumMixinItemRendererMixin" -> isUsingSodium;
            // Disable ItemOverrides changes if Chime is being used (which changes item overrides)
            case "com.noxcrew.noxesium.mixin.performance.model.ItemOverridesMixin" -> !isUsingChime;
            // We don't disable the other beacon patches as they simply get made useless by Sodium removing
            // all default global block entities.
            default -> true;
        };
    }

    @Override
    public void onLoad(String mixinPackage) {
        isUsingSodium = FabricLoader.getInstance().isModLoaded("sodium");
        isUsingIris = FabricLoader.getInstance().isModLoaded("iris");
        isUsingChime = FabricLoader.getInstance().isModLoaded("chime");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
