package com.noxcrew.noxesium.sync.network;

/**
 * Stores information on a singular file being synchronized.
 *
 * @param path       The path of the file relative to the root.
 * @param part       The index of this part of the packet.
 * @param total      The total amount of parts.
 * @param modifyTime The last time when this file was modified.
 * @param content    The content of this part.
 */
public record SyncedPart(String path, int part, int total, long modifyTime, byte[] content) {}
