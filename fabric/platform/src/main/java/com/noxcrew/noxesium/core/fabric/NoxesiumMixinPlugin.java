package com.noxcrew.noxesium.core.fabric;

import com.noxcrew.noxesium.api.NoxesiumApi;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Enables certain mixins based on whether other mods are present or not.
 */
public class NoxesiumMixinPlugin implements IMixinConfigPlugin {

    private static final String PREFIX = "com.noxcrew.noxesium.core.fabric.mixin.";
    private static final String SODIUM_PREFIX = "com.noxcrew.noxesium.core.fabric.mixin.sodium.";
    private boolean isUsingSodium;

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(PREFIX)) {
            NoxesiumApi.getLogger()
                    .error("Skipping mixin class {} as it's outside Noxesium's mixin package", mixinClassName);
            return false;
        }
        if (mixinClassName.startsWith(SODIUM_PREFIX)) return isUsingSodium;
        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {
        isUsingSodium = FabricLoader.getInstance().isModLoaded("sodium");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
