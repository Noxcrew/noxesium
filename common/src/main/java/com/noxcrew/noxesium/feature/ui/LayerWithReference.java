package com.noxcrew.noxesium.feature.ui;

import com.noxcrew.noxesium.feature.ui.layer.NoxesiumLayer;

import java.util.List;

/**
 * Holds a layer and a reference to its group.
 */
public record LayerWithReference(
        int index,
        NoxesiumLayer.Layer layer,
        List<NoxesiumLayer.NestedLayers> groups
) {
}
