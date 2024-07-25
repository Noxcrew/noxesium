package com.noxcrew.noxesium.mixin.sodium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.NoxesiumMod;
import me.jellysquid.mods.sodium.client.gl.GlObject;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL32C;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.IntFunction;

/**
 * Directly based on <a href="https://github.com/lni-dev/SodiumCoreShaderSupport/blob/1.20.6/src/main/java/de/linusdev/mixin/MixinGLProgram.java">Sodium Core Shader Support</a>.
 */
@Mixin(value = GlProgram.class, remap = false)
public class SodiumGlProgramMixin extends GlObject {

    @WrapMethod(method = "bindUniform")
    private <U extends GlUniform<?>> U noxesium$bindUniform(String name, IntFunction<U> factory, Operation<U> original) {
        var index = GL20C.glGetUniformLocation(this.handle(), name);
        if (index < 0) {
            var error = GL20C.glGetError();
            if (error == GL20C.GL_INVALID_OPERATION) {
                NoxesiumMod.getInstance().getLogger().warn("Error while binding uniform: GL_INVALID_OPERATION");
            } else if (error == GL20C.GL_INVALID_VALUE) {
                NoxesiumMod.getInstance().getLogger().warn("Error while binding uniform: GL_INVALID_VALUE");
            } else {
                NoxesiumMod.getInstance().getLogger().warn("Unknown error while binding uniform, code: {}", error);
            }
        }
        return (U) factory.apply(index);
    }

    @WrapMethod(method = "bindUniformBlock")
    private GlUniformBlock noxesium$bindUniformBlock(String name, int bindingPoint, Operation<GlUniformBlock> original) {
        var index = GL32C.glGetUniformBlockIndex(this.handle(), name);
        if (index < 0) {
            var error = GL20C.glGetError();
            if (error == GL20C.GL_INVALID_OPERATION) {
                NoxesiumMod.getInstance().getLogger().warn("Error while binding uniform block: GL_INVALID_OPERATION");
            } else if (error == GL20C.GL_INVALID_VALUE) {
                NoxesiumMod.getInstance().getLogger().warn("Error while binding uniform block: GL_INVALID_VALUE");
            } else {
                NoxesiumMod.getInstance().getLogger().warn("Unknown error while binding uniform block, code: {}", error);
            }
        }
        GL32C.glUniformBlockBinding(this.handle(), index, bindingPoint);
        return new GlUniformBlock(bindingPoint);
    }
}
