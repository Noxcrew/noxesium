package com.noxcrew.noxesium.core.fabric.mixin.rules.qib;

import com.noxcrew.noxesium.core.fabric.feature.entity.InteractionExtension;
import com.noxcrew.noxesium.core.fabric.feature.entity.SpatialInteractionEntityTree;
import net.minecraft.world.entity.Interaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Hooks into the interaction entity code and implements [InteractionExtension].
 */
@Mixin(Interaction.class)
public abstract class InteractionExtensionMixin implements InteractionExtension {

    @Unique
    private boolean noxesium$initialized = false;

    @Override
    public boolean noxesium$isInWorld() {
        return noxesium$initialized;
    }

    @Override
    public void noxesium$markAddedToWorld() {
        noxesium$initialized = true;

        // When we first add an interaction entity to the world we need
        // to add it to the spatial tree to ensure it's included as it
        // might never get moved from its initial location after this point.
        // We don't included any entities before they have been added
        // to the world as they will instantly get removed!
        if (((Object) this) instanceof Interaction interaction) {
            SpatialInteractionEntityTree.update(interaction);
        }
    }
}
