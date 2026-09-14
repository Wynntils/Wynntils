/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.wynntils.utils.type.BoundingBox;
import java.util.ArrayList;
import java.util.List;
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

    public void reveal(double worldX, double worldZ, int radiusChunks) {
        int chunkX = toChunk(worldX);
        int chunkZ = toChunk(worldZ);
        for (int x = chunkX - radiusChunks; x <= chunkX + radiusChunks; x++) {
            for (int z = chunkZ - radiusChunks; z <= chunkZ + radiusChunks; z++) {
                chunks.add(chunkKey(x, z));
            }
        }
    }

    public boolean isDiscovered(double worldX, double worldZ) {
        return chunks.contains(chunkKey(toChunk(worldX), toChunk(worldZ)));
    }

    /** Discovered area inside the box as one world-space rect per horizontal run of discovered chunks. */
    public List<BoundingBox> discoveredRuns(BoundingBox box) {
        List<BoundingBox> runs = new ArrayList<>();
        int minChunkX = toChunk(box.x1());
        int maxChunkX = toChunk(Math.nextDown(box.x2()));
        int minChunkZ = toChunk(box.z1());
        int maxChunkZ = toChunk(Math.nextDown(box.z2()));

        for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
            int runStart = Integer.MIN_VALUE;
            for (int chunkX = minChunkX; chunkX <= maxChunkX + 1; chunkX++) {
                boolean discovered = chunkX <= maxChunkX && chunks.contains(chunkKey(chunkX, chunkZ));
                if (discovered && runStart == Integer.MIN_VALUE) {
                    runStart = chunkX;
                } else if (!discovered && runStart != Integer.MIN_VALUE) {
                    runs.add(new BoundingBox(
                            Math.max(box.x1(), runStart << 4),
                            Math.max(box.z1(), chunkZ << 4),
                            Math.min(box.x2(), chunkX << 4),
                            Math.min(box.z2(), (chunkZ + 1) << 4)));
                    runStart = Integer.MIN_VALUE;
                }
            }
        }
        return runs;
    }

    private static int toChunk(double world) {
        return (int) Math.floor(world) >> 4;
    }

    // Same packing as ChunkPos.asLong, without triggering Minecraft's registry bootstrap
    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX & 0xFFFFFFFFL) | (((long) chunkZ & 0xFFFFFFFFL) << 32);
    }
}
