package com.noxcrew.noxesium.mixin.sodium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.NoxesiumMod;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;

import java.nio.charset.StandardCharsets;

/**
 * Based on <a href="https://github.com/lni-dev/SodiumCoreShaderSupport/blob/1.20.6/src/main/java/de/linusdev/mixin/MixinShaderLoader.java">Sodium Core Shader Support</a>.
 * <p>
 * Sodium has indicated they do not want to add core shader support natively, the discussion of why is best left to other places.
 * But in practice this leaves server developers that want to use core shaders with half their players being unable to see them. So, Noxesium
 * lets you edit the shaders but acknowledges that it is entirely the responsibility of the server developer to fix any issues that occur.
 * <p>
 * Using Sodium's core shaders on a public server is irresponsible and entirely your problem if it causes issues. This system is meant for
 * private servers where you can guarantee the version clients are using. But I'm a code comment, not a cop, I can't stop you.
 */
@Mixin(value = ShaderLoader.class, remap = false)
public class SodiumShaderLoaderMixin {

    @WrapMethod(method = "getShaderSource")
    private static String noxesium$getShaderSource(ResourceLocation name, Operation<String> original) {
        // Determine if this shader is being provided by some resource pack, we fall back to Sodium
        // if anything goes wrong while doing so!
        var cache = NoxesiumMod.getInstance().getCachedShaders();
        var resource = cache != null ? cache.cache().get(name) : null;
        if (resource != null) {
            try {
                return IOUtils.toString(resource.open(), StandardCharsets.UTF_8);
            } catch (Exception x) {
                NoxesiumMod.getInstance().getLogger().error("Exception while reading shader for source {} from resource pack", name);
            }
        }
        return original.call(name);
    }
}
