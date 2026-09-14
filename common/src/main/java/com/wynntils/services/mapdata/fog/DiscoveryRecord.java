/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.fog;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The set of discovery cells (chunks) one character has been near.
 * The backing set is concurrent because storage serialises it on a background thread while the game thread reveals.
 */
public final class DiscoveryRecord {
    private final Set<Long> chunks;

    public DiscoveryRecord() {
        this(Set.of());
    }

    public DiscoveryRecord(Set<Long> chunks) {
        this.chunks = ConcurrentHashMap.newKeySet();
        this.chunks.addAll(chunks);
    }

    public Set<Long> chunks() {
        return chunks;
    }

    /** @return whether any new chunk was discovered */
    public boolean reveal(double worldX, double worldZ, int radiusChunks, RevealShape shape) {
        int chunkX = toChunk(worldX);
        int chunkZ = toChunk(worldZ);
        boolean changed = false;
        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                if (!shape.contains(dx, dz, radiusChunks)) continue;
                changed |= chunks.add(chunkKey(chunkX + dx, chunkZ + dz));
            }
        }
        return changed;
    }

    public void clear() {
        chunks.clear();
    }

    public boolean isDiscovered(double worldX, double worldZ) {
        return isDiscoveredChunk(toChunk(worldX), toChunk(worldZ));
    }

    public boolean isDiscoveredChunk(int chunkX, int chunkZ) {
        return chunks.contains(chunkKey(chunkX, chunkZ));
    }

    private static int toChunk(double world) {
        return (int) Math.floor(world) >> 4;
    }

    // Same packing as ChunkPos.asLong, without triggering Minecraft's registry bootstrap
    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX & 0xFFFFFFFFL) | (((long) chunkZ & 0xFFFFFFFFL) << 32);
    }
}
