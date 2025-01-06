package com.noxcrew.noxesium.feature.ui.render.api;

/**
 * Stores a buffer's texture and its desired blend state.
 */
public record BufferData(int textureId, BlendState state) {}
