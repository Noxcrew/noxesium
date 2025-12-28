package com.noxcrew.noxesium.core.fabric.feature.entity;

import com.mojang.datafixers.util.Pair;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.qib.SpatialTree;
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import java.util.HashSet;
import java.util.Set;
import net.kyori.adventure.key.Key;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * Stores a spatial tree with the locations of all interaction entities.
 */
public class ClientSpatialInteractionEntityTree extends SpatialTree {
    private HashSet<Pair<Key, AABB>> modelContents = new HashSet<>();

    public ClientSpatialInteractionEntityTree() {
        super(new EntityMBRConverter());
        staticModel.load(Set.of());
    }

    /**
     * Returns the contents of the model, if debugging is active.
     */
    public Set<Pair<Key, AABB>> getModelContents() {
        return modelContents;
    }

    @Override
    public @Nullable Entity getEntity(int entityId) {
        return Minecraft.getInstance().level.getEntity(entityId);
    }

    @Override
    public void rebuild() {
        if (!needsRebuilding.get() || rebuilding.get()) return;

        // Ensure the world exists before calling the super-method as it calls getEntity!
        var world = Minecraft.getInstance().level;
        if (world == null) return;

        var oldStaticEntities = staticEntities.size();
        var oldAddedEntities = pendingEntities.size();
        var oldRemovingEntities = removedEntities.size();
        super.rebuild();

        if (NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging) {
            if (Minecraft.getInstance().player != null) {
                var message = Component.literal("§eRebuilt spatial model, before: §f[S" + oldStaticEntities + ", A"
                        + oldAddedEntities + ", R" + oldRemovingEntities + "]§e, after: §f[S"
                        + staticEntities.size() + ", A" + pendingEntities.size() + ", R"
                        + removedEntities.size() + "]");
                NoxesiumMod.getInstance().ensureMain(() -> {
                    Minecraft.getInstance().getChatListener().handleSystemMessage(message, false);
                });
            }

            var newContents = new HashSet<Pair<Key, AABB>>();
            for (var entity : staticEntities) {
                var fetched = world.getEntity(entity);
                if (fetched == null) continue;
                var type = fetched.noxesium$getComponent(CommonEntityComponentTypes.QIB_BEHAVIOR);
                if (type == null) continue;
                newContents.add(Pair.of(type, fetched.getBoundingBox()));
            }
            modelContents = newContents;
        }
    }
}
