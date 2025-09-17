package com.noxcrew.noxesium.core.fabric.feature.entity;

import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.core.qib.QibCollisionManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

/**
 * Applies qib behaviors whenever players clip interaction entities.
 */
public class QibBehaviorModule extends NoxesiumFeature {

    private final ClientSpatialInteractionEntityTree spatialTree = new ClientSpatialInteractionEntityTree();
    private QibCollisionManager qibCollisionManager;

    public QibBehaviorModule() {
        ClientTickEvents.END_CLIENT_TICK.register((ignored) -> {
            // Ignore unless registered!
            if (!isRegistered()) return;

            // If there are no qib behaviors set, do nothing!
            if (NoxesiumRegistries.QIB_EFFECTS.isEmpty()) return;

            // Perform checks in between the last and next bounding box
            var player = Minecraft.getInstance().player;
            if (player == null) return;

            // Tick the collision manager
            if (qibCollisionManager == null) {
                qibCollisionManager = new ClientQibCollisionManager(player, spatialTree);
            }
            qibCollisionManager.tick();
        });
    }

    /**
     * Returns the spatial tree.
     */
    public ClientSpatialInteractionEntityTree getSpatialTree() {
        return spatialTree;
    }

    /**
     * Triggers when a player jumps.
     */
    public void onPlayerJump() {
        if (qibCollisionManager != null) {
            qibCollisionManager.onPlayerJump();
        }
    }
}
