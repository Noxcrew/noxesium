package com.noxcrew.noxesium.core.fabric.registry;

import com.noxcrew.noxesium.api.component.ComponentChangeContext;
import com.noxcrew.noxesium.api.component.NoxesiumComponentListener;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.fabric.mixin.rules.mouse.MouseHandlerExt;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/**
 * Registers listeners for component changes.
 */
public class ComponentChangeListeners extends NoxesiumFeature {
    public ComponentChangeListeners() {
        listenEntity(CommonEntityComponentTypes.HITBOX_OVERRIDE, (ignored, context) -> {
            // Update the bounding box of the entity whenever a new override is received
            var entity = context.receiver();
            entity.setBoundingBox(entity.makeBoundingBox());
        });

        listenGame(CommonGameComponentTypes.CAMERA_LOCKED, (ignored, context) -> {
            if (context.oldValue() != null && context.newValue() == null) {
                // Remove all accumulated mouse movement whenever the camera stops being locked
                var mouseHandler = (MouseHandlerExt) Minecraft.getInstance().mouseHandler;
                mouseHandler.setAccumulatedDeltaX(0.0);
                mouseHandler.setAccumulatedDeltaY(0.0);
            }
        });
        listenGame(CommonGameComponentTypes.DISABLE_VANILLA_MUSIC, (ignored, context) -> {
            // If background music is playing, and we've just enabled custom music, stop it!
            // This prevents all vanilla music from playing on servers with custom music.
            if (context.newValue() != null) {
                Minecraft.getInstance().getMusicManager().stopPlaying();
            }
        });
        listenGame(CommonGameComponentTypes.OVERRIDE_GRAPHICS_MODE, (ignored, context) -> {
            // We need to call this whenever we change the display type.
            if (Minecraft.getInstance().levelRenderer != null) {
                Minecraft.getInstance().levelRenderer.allChanged();
            }
        });
        listenGame(CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS, (ignored, context) -> {
            // Save any existing options before we override them.
            if (Minecraft.getInstance().options != null) {
                Minecraft.getInstance().options.save();
            }

            // We need to call this when hitboxes & chunk boundaries are updated.
            if (Minecraft.getInstance().levelRenderer != null) {
                Minecraft.getInstance().levelRenderer.allChanged();
            }
        });
        listenGame(CommonGameComponentTypes.CUSTOM_CREATIVE_ITEMS, (ignored, context) -> {
            // Mark that the creative tab has changed so it is updated.
            NoxesiumMod.getInstance().hasCreativeTabChanged = true;
        });
    }

    /**
     * Listens to an entity component being modified.
     */
    private <T> void listenEntity(
            NoxesiumComponentType<T> type,
            BiConsumer<ComponentChangeListeners, ComponentChangeContext<T, Entity>> consumer) {
        if (type.listener() == null) return;
        ((NoxesiumComponentListener<T, Entity>) type.listener()).addListener(this, (reference, context) -> {
            if (isRegistered()) {
                consumer.accept(reference, context);
            }
        });
    }

    /**
     * Listens to a game component being modified.
     */
    private <T> void listenGame(
            NoxesiumComponentType<T> type,
            BiConsumer<ComponentChangeListeners, ComponentChangeContext<T, Minecraft>> consumer) {
        if (type.listener() == null) return;
        ((NoxesiumComponentListener<T, Minecraft>) type.listener()).addListener(this, (reference, context) -> {
            if (isRegistered()) {
                consumer.accept(reference, context);
            }
        });
    }
}
