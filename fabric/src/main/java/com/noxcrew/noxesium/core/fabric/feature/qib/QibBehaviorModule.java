package com.noxcrew.noxesium.core.fabric.feature.qib;

import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.fabric.util.BackgroundTaskFeature;
import com.noxcrew.noxesium.core.nms.feature.qib.QibCollisionManager;
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.kyori.adventure.key.Key;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;

/**
 * Applies qib behaviors whenever players clip interaction entities.
 */
public class QibBehaviorModule extends NoxesiumFeature implements BackgroundTaskFeature {

    private final ClientSpatialInteractionEntityTree spatialTree = new ClientSpatialInteractionEntityTree();
    private QibCollisionManager qibCollisionManager;

    public QibBehaviorModule() {
        ClientTickEvents.END_CLIENT_TICK.register((ignored) -> {
            // Ignore unless registered and there being qibs!
            if (!isRegistered() || NoxesiumRegistries.QIB_EFFECTS.isEmpty()) {
                // Destroy the manager instance!
                qibCollisionManager = null;
                return;
            }

            // Perform checks in between the last and next bounding box
            var player = Minecraft.getInstance().player;
            if (player == null) {
                // Destroy the manager as the player is gone!
                qibCollisionManager = null;
                return;
            }

            // Check that the player reference is valid
            if (qibCollisionManager != null && qibCollisionManager.getPlayer() != player) {
                qibCollisionManager = null;
            }

            // Tick the collision manager
            if (qibCollisionManager == null) {
                qibCollisionManager = new ClientQibCollisionManager(player, spatialTree);
            }
            qibCollisionManager.tick();
        });
    }

    @Override
    public void runAsync() {
        spatialTree.rebuild();
    }

    @Override
    public void onRegister() {
        refresh();
    }

    @Override
    public void onTransfer() {
        refresh();
    }

    /**
     * Refreshes the spatial tree, adding any entities to it that are in the world but not
     * in the tree.
     */
    public void refresh() {
        spatialTree.clear();

        var oldAddedEntities = spatialTree.getPendingEntities().size();
        var oldRemovingEntities = spatialTree.getRemovedEntities().size();

        // Go through the level and detect all entities that already have qib data somehow!
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        for (var entity : level.getEntities().getAll()) {
            if (entity instanceof Interaction interaction
                    && interaction.noxesium$hasComponent(CommonEntityComponentTypes.QIB_BEHAVIOR)) {
                spatialTree.update(interaction);
            }
        }

        if (NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging) {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance()
                        .getChatListener()
                        .handleSystemMessage(
                                Component.literal("§eRefreshed spatial model, before: §f[A"
                                        + oldAddedEntities + ", R" + oldRemovingEntities + "]§e, after: §f[A"
                                        + spatialTree.getPendingEntities().size() + ", R"
                                        + spatialTree.getRemovedEntities().size() + "]"),
                                false);
            }
        }
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

    /**
     * Uses the given behavior for this player.
     */
    public void useItemBehavior(Player player, Key behavior) {
        if (qibCollisionManager != null) {
            qibCollisionManager.onUseItemBehavior(player, behavior);
        }
    }
}
