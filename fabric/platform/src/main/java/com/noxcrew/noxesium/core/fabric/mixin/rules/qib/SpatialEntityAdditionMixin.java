package com.noxcrew.noxesium.core.fabric.mixin.rules.qib;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into an entity being added to the world and calls the mark in world extension.
 */
@Mixin(ClientLevel.class)
public abstract class SpatialEntityAdditionMixin {

    @Inject(method = "addEntity", at = @At("RETURN"))
    public void onAddEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof Interaction interaction) {
            interaction.noxesium$markAddedToWorld();
        }
    }
}
