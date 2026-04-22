package com.noxcrew.noxesium.core.fabric.mixin.feature.debugoptions;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.core.feature.DebugOption;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Shadow
    @Final
    private List<DebugRenderer.SimpleDebugRenderer> renderers;

    @WrapOperation(
            method = "refreshRendererList",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;isCurrentlyEnabled(Lnet/minecraft/resources/Identifier;)Z"))
    private boolean restrictDebugRendering(DebugScreenEntryList instance, Identifier id, Operation<Boolean> original) {
        if (!original.call(instance, id)) return false;
        var restrictedOptions =
                GameComponents.getInstance().noxesium$getComponent(CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS);
        if (restrictedOptions == null) return true;

        if (Objects.equals(id, DebugScreenEntries.CHUNK_BORDERS)) {
            return !restrictedOptions.contains(DebugOption.CHUNK_BOUNDARIES.getKeyCode());
        }
        if (Objects.equals(id, DebugScreenEntries.ENTITY_HITBOXES)) {
            return !restrictedOptions.contains(DebugOption.SHOW_HITBOXES.getKeyCode());
        }
        return true;
    }

    @WrapMethod(method = "refreshRendererList", order = 1000000)
    private void refreshRendererList(Operation<Void> original) {
        if (GameComponents.getInstance().noxesium$hasComponent(CommonGameComponentTypes.DISABLE_ALL_DEBUG_RENDERERS)) {
            // If no renderers are allowed, clear the list and ignore the rest of the event!
            renderers.clear();
            return;
        }
        original.call();
    }
}
