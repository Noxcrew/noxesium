package com.noxcrew.noxesium.feature.ui.render.buffer;

import com.noxcrew.noxesium.feature.ui.render.api.BlendState;

/**
 * Stores a buffer's texture and its desired blend state.
 */
public record BufferData(int textureId, BlendState state) {}
