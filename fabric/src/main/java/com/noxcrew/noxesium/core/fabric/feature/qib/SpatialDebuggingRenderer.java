package com.noxcrew.noxesium.core.fabric.feature.qib;

import com.noxcrew.noxesium.api.NoxesiumApi;
import java.awt.Color;
import java.util.Random;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.debug.DebugValueAccess;

/**
 * Helps in debugging the spatial interaction tree.
 */
public class SpatialDebuggingRenderer implements DebugRenderer.SimpleDebugRenderer {

    @Override
    public void emitGizmos(
            double cameraX,
            double cameraY,
            double cameraZ,
            DebugValueAccess debugValueAccess,
            Frustum frustum,
            float v3) {
        // Get all models to render
        var models = NoxesiumApi.getInstance()
                .getFeatureOptional(QibBehaviorModule.class)
                .map(module -> module.getSpatialTree().getModelContents())
                .orElse(null);
        if (models == null) return;

        // First render line boxes at render offset
        for (var pair : models) {
            var seededRandom = new Random(pair.getFirst().hashCode());
            var color = new Color(seededRandom.nextInt());
            Gizmos.cuboid(pair.getSecond(), GizmoStyle.stroke(color.getRGB()));
        }

        // Render floating texts afterwards as they don't need to be offset
        for (var pair : models) {
            // Skip if there is no text!
            if (pair.getFirst().asString().isBlank()) continue;

            var center = pair.getSecond().getCenter();
            Gizmos.billboardText(pair.getFirst().asString(), center, TextGizmo.Style.whiteAndCentered());
        }
    }
}
