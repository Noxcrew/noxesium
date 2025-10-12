package com.noxcrew.noxesium.core.fabric.feature.entity;

import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.qib.SpatialTree;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * Stores a spatial tree with the locations of all interaction entities.
 */
public class ClientSpatialInteractionEntityTree extends SpatialTree {
    private HashSet<AABB> modelContents = new HashSet<>();

    public ClientSpatialInteractionEntityTree() {
        super(new EntityMBRConverter());
        staticModel.load(Set.of());
    }

    /**
     * Returns the contents of the model, if debugging is active.
     */
    public Set<AABB> getModelContents() {
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
                Minecraft.getInstance()
                        .getChatListener()
                        .handleSystemMessage(
                                Component.literal("§eRebuilt spatial model, before: §f[S" + oldStaticEntities + ", A"
                                        + oldAddedEntities + ", R" + oldRemovingEntities + "]§e, after: §f[S"
                                        + staticEntities.size() + ", A" + pendingEntities.size() + ", R"
                                        + removedEntities.size() + "]"),
                                false);
            }

            var newContents = new HashSet<AABB>();
            for (var entity : staticEntities) {
                var fetched = world.getEntity(entity);
                if (fetched == null) continue;
                newContents.add(fetched.getBoundingBox());
            }
            modelContents = newContents;
        }
    }
}
