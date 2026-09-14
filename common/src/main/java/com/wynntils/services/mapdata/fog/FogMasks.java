/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.fog;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.wynntils.services.map.MapTexture;
import com.wynntils.utils.mc.McUtils;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * One GPU mask texture per map tile, one texel per chunk, red = discovered. Sampled with LINEAR filtering by
 * FOG_MASK_PIPELINE so fog fades over a chunk. Masks are rebuilt lazily whenever {@link #invalidate()} was called.
 */
public final class FogMasks {
    private static final int CHUNK_SIZE = 16;
    // One chunk of padding on every side, filled from neighbouring tiles' chunks, so the fade is
    // continuous across tile seams instead of clamping at each tile's edge
    public static final int PADDING_BLOCKS = CHUNK_SIZE;
    private static final int DISCOVERED = 0xFFFFFFFF;
    private static final int UNDISCOVERED = 0xFF000000;

    private final Map<Identifier, DynamicTexture> masks = new HashMap<>();
    private final Map<Identifier, Integer> builtVersions = new HashMap<>();

    private int version = 0;

    public void invalidate() {
        version++;
    }

    public Identifier maskFor(MapTexture map, DiscoveryRecord record) {
        Identifier identifier = Identifier.fromNamespaceAndPath(
                "wynntils", "/fog" + map.identifier().getPath());
        DynamicTexture mask = masks.get(identifier);
        if (mask == null) {
            mask = register(identifier, map);
            masks.put(identifier, mask);
        }
        if (builtVersions.getOrDefault(identifier, -1) != version) {
            fill(mask, map, record);
            builtVersions.put(identifier, version);
        }
        return identifier;
    }

    private static int maskSize(int blocks) {
        return Math.ceilDiv(blocks, CHUNK_SIZE) + 2;
    }

    private static DynamicTexture register(Identifier identifier, MapTexture map) {
        DynamicTexture mask = new DynamicTexture(
                identifier::toString, maskSize(map.getTextureWidth()), maskSize(map.getTextureHeight()), false);
        mask.sampler = RenderSystem.getSamplerCache()
                .getSampler(
                        AddressMode.CLAMP_TO_EDGE,
                        AddressMode.CLAMP_TO_EDGE,
                        FilterMode.LINEAR,
                        FilterMode.LINEAR,
                        false);
        McUtils.mc().getTextureManager().register(identifier, mask);
        return mask;
    }

    private static void fill(DynamicTexture mask, MapTexture map, DiscoveryRecord record) {
        // Mask texels line up with the tile only when the tile origin is chunk-aligned; every tile in maps.json is
        assert map.getX1() % CHUNK_SIZE == 0 && map.getZ1() % CHUNK_SIZE == 0;
        int firstChunkX = Math.floorDiv(map.getX1(), CHUNK_SIZE) - 1;
        int firstChunkZ = Math.floorDiv(map.getZ1(), CHUNK_SIZE) - 1;
        for (int z = 0; z < mask.getPixels().getHeight(); z++) {
            for (int x = 0; x < mask.getPixels().getWidth(); x++) {
                boolean discovered = record.isDiscoveredChunk(firstChunkX + x, firstChunkZ + z);
                mask.getPixels().setPixelABGR(x, z, discovered ? DISCOVERED : UNDISCOVERED);
            }
        }
        mask.upload();
    }
}
