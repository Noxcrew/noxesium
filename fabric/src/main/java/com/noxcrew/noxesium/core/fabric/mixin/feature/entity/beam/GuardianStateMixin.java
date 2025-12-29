package com.noxcrew.noxesium.core.fabric.mixin.feature.entity.beam;

import com.noxcrew.noxesium.core.fabric.feature.entity.BeamColorStateExtension;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Implements the beam color state extension.
 */
@Mixin(value = {GuardianRenderState.class, EndCrystalRenderState.class})
public class GuardianStateMixin implements BeamColorStateExtension {

    @Unique
    private Integer noxesium$baseColor;

    @Unique
    private Integer noxesium$fadeColor;

    @Override
    public Integer noxesium$getBeamColor() {
        return noxesium$baseColor;
    }

    @Override
    public Integer noxesium$getBeamColorFade() {
        return noxesium$fadeColor;
    }

    @Override
    public void noxesium$setBeamColor(Integer color, Integer fade) {
        this.noxesium$baseColor = color;
        this.noxesium$fadeColor = fade;
    }
}
