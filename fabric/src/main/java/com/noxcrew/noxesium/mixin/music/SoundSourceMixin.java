package com.noxcrew.noxesium.mixin.music;

import net.minecraft.sounds.SoundSource;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Hooks into the SoundSource enum to add additional custom values.
 *
 * Credits to <a href="https://github.com/AsoDesu/IslandUtils/blob/main/src/main/java/net/asodev/islandutils/mixins/sounds/SoundSourceMixin.java">AsoDesu</a>
 * for the concept of injecting into the $VALUES initialisation.
 */
// Ensure we inject after other mods
@Mixin(value = SoundSource.class, priority = 1001)
public class SoundSourceMixin {

    @Mutable @Shadow @Final private static SoundSource[] $VALUES;

    @Invoker("<init>")
    private static SoundSource createNewCategory(String fieldName, int ordinal, String name) {
        throw new AssertionError("Unimplemented");
    }

    @Inject(
            method = "<clinit>",
            at = @At(value = "FIELD", target = "Lnet/minecraft/sounds/SoundSource;$VALUES:[Lnet/minecraft/sounds/SoundSource;", opcode = Opcodes.PUTSTATIC, shift = At.Shift.AFTER)
    )
    private static void modifyValues(CallbackInfo ci) {
        var newValues = new ArrayList<>(List.of($VALUES));
        newValues.add(createNewCategory("CORE_MUSIC", newValues.size(), "core_music"));
        newValues.add(createNewCategory("GAME_MUSIC", newValues.size(), "game_music"));
        $VALUES = newValues.toArray(new SoundSource[0]);
    }
}
